package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.core.remote.test
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.coroutines.session.acceptorSessionFactory
import ch.softappeal.yass2.coroutines.session.initiatorSessionFactory
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.readByte
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun runServer(block: suspend CoroutineScope.(tcp: TcpSocketBuilder, serverSocket: ServerSocket) -> Unit) {
    runBlocking {
        SelectorManager().use { selector ->
            val tcp = aSocket(selector).tcp()
            tcp.bind(LOCAL_HOST, PORT).use { serverSocket -> block(tcp, serverSocket) }
        }
    }
}

class SocketTest {
    @Test
    fun closeSocket() {
        runServer { tcp, serverSocket ->
            val acceptedSocketDeferred = async { serverSocket.accept() }
            val clientSocket = tcp.connect(serverSocket.localAddress)
            val acceptedSocket = acceptedSocketDeferred.await()
            assertFalse(acceptedSocket.isClosed)
            val clientByte = async { clientSocket.openReadChannel().readByte() }
            assertTrue(clientByte.isActive)
            assertFalse(clientSocket.isClosed)
            // the following line closes clientSocket on jvm and native,
            // see https://youtrack.jetbrains.com/issue/KTOR-5093/Native-Read-from-a-closed-socket-doesnt-throw-an-exception
            clientSocket.cancel()
            assertFailsWith<CancellationException> { clientByte.await() }
            assertTrue(clientSocket.isClosed)
            acceptedSocket.close()
            assertTrue(acceptedSocket.isClosed)
        }
    }

    @Test
    fun socket() {
        runServer { tcp, serverSocket ->
            val listenerJob = launch {
                val serverTunnel = tunnel { currentCoroutineContext()[SocketCce]!!.socket.remoteAddress }
                while (true) {
                    val socket = serverSocket.accept()
                    launch {
                        socket.handleRequest(ContractTransport, serverTunnel)
                    }
                }
            }
            try {
                val clientTunnel = ContractTransport.tunnel { tcp.connect(serverSocket.localAddress) }
                clientTunnel.test()
            } finally {
                listenerJob.cancel()
            }
        }
    }

    @Test
    fun socketSession() {
        runServer { tcp, serverSocket ->
            val acceptorJob = launch {
                while (true) {
                    val socket = serverSocket.accept()
                    launch {
                        socket.receiveLoop(
                            ContractTransport,
                            acceptorSessionFactory { connection.socket.remoteAddress }
                        )
                    }
                }
            }
            try {
                tcp.connect(serverSocket.localAddress)
                    .receiveLoop(ContractTransport, initiatorSessionFactory())
            } finally {
                acceptorJob.cancel()
            }
        }
    }
}

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.session.acceptorSessionFactory
import ch.softappeal.yass2.session.initiatorSessionFactory
import ch.softappeal.yass2.session.test
import ch.softappeal.yass2.session.tunnel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

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
            val acceptedSocket = async { serverSocket.accept() }
            val clientSocket = tcp.connect(serverSocket.localAddress)
            assertFalse(acceptedSocket.await().isClosed)
            val clientByte = async { clientSocket.openReadChannel().readByte() }
            assertTrue(clientByte.isActive)
            assertFalse(clientSocket.isClosed)
            // the following line closes clientSocket on jvm and native,
            // see https://youtrack.jetbrains.com/issue/KTOR-5093/Native-Read-from-a-closed-socket-doesnt-throw-an-exception
            clientSocket.cancel()
            assertFailsWith<CancellationException> { clientByte.await() }
            assertTrue(clientSocket.isClosed)
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
                delay(100.milliseconds) // give some time to shut down
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
                delay(500.milliseconds) // give some time to shut down
                acceptorJob.cancel()
            }
        }
    }
}

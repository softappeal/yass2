package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.coroutines.session.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.test.*

private fun runServer(block: suspend CoroutineScope.(tcp: TcpSocketBuilder, serverSocket: ServerSocket) -> Unit) {
    SelectorManager().use { selector ->
        val tcp = aSocket(selector).tcp()
        tcp.bind().use { serverSocket ->
            runBlocking { block(tcp, serverSocket) }
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
            (clientSocket as CoroutineScope).cancel() // TODO: remove cast if this is fixed in Ktor
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
                        socket.handleRequest(MessageTransport, serverTunnel)
                    }
                }
            }
            try {
                val clientTunnel = MessageTransport.socketTunnel { tcp.connect(serverSocket.localAddress) }
                clientTunnel.test(100)
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
                            PacketTransport,
                            acceptorSessionFactory { (connection as SocketConnection).socket.remoteAddress }
                        )
                    }
                }
            }
            try {
                tcp.connect(serverSocket.localAddress)
                    .receiveLoop(PacketTransport, initiatorSessionFactory(1000))
            } finally {
                delay(100) // give some time for closing of socket
                acceptorJob.cancel()
            }
        }
    }
}

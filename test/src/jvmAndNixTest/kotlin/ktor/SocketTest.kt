package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.contract.createStringEncoders
import ch.softappeal.yass2.coroutines.acceptorSessionFactory
import ch.softappeal.yass2.coroutines.initiatorSessionFactory
import ch.softappeal.yass2.coroutines.test
import ch.softappeal.yass2.coroutines.tunnel
import ch.softappeal.yass2.serialize.Transport
import ch.softappeal.yass2.serialize.string.TextSerializer
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

private const val ENABLE_SOCKET_TEST = false // TODO: spurious failures

private fun runServer(block: suspend CoroutineScope.(tcp: TcpSocketBuilder, serverSocket: ServerSocket) -> Unit) {
    if (!ENABLE_SOCKET_TEST) return
    runBlocking {
        SelectorManager().use { selector ->
            val tcp = aSocket(selector).tcp()
            tcp.bind().use { serverSocket -> block(tcp, serverSocket) }
        }
    }
}

private val TextTransport = Transport(TextSerializer(createStringEncoders()))

private fun socketTest(transport: Transport) {
    runServer { tcp, serverSocket ->
        val listenerJob = launch {
            val serverTunnel = tunnel { currentCoroutineContext()[SocketCce]!!.socket.remoteAddress }
            while (true) {
                val socket = serverSocket.accept()
                launch {
                    socket.handleRequest(transport, serverTunnel)
                }
            }
        }
        try {
            val clientTunnel = transport.tunnel { tcp.connect(serverSocket.localAddress) }
            clientTunnel.test(100)
        } finally {
            delay(100.milliseconds) // give some time to shut down
            listenerJob.cancel()
        }
    }
}

private fun socketSessionTest(transport: Transport) {
    runServer { tcp, serverSocket ->
        val acceptorJob = launch {
            while (true) {
                val socket = serverSocket.accept()
                launch {
                    socket.receiveLoop(
                        transport,
                        acceptorSessionFactory { connection.socket.remoteAddress }
                    )
                }
            }
        }
        try {
            tcp.connect(serverSocket.localAddress)
                .receiveLoop(transport, initiatorSessionFactory(1000))
        } finally {
            delay(500.milliseconds) // give some time to shut down
            acceptorJob.cancel()
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
        socketTest(ContractTransport)
    }

    @Test
    fun textSocket() {
        socketTest(TextTransport)
    }

    @Test
    fun socketSession() {
        socketSessionTest(ContractTransport)
    }

    @Test
    fun textSocketSession() {
        socketSessionTest(TextTransport)
    }
}

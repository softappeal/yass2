@file:OptIn(InternalApi::class, ExperimentalApi::class)

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.remote.invoke
import ch.softappeal.yass2.coroutines.session.acceptorSessionFactory
import ch.softappeal.yass2.coroutines.session.initiatorSessionFactory
import ch.softappeal.yass2.coroutines.session.tunnelWithContext
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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val IGNORE = false // TODO: spurious failures in SocketTest

private fun runServer(block: suspend CoroutineScope.(tcp: TcpSocketBuilder, serverSocket: ServerSocket) -> Unit) {
    if (IGNORE) return
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
            clientSocket.cancel() // closes clientSocket, see https://youtrack.jetbrains.com/issue/KTOR-5093/Native-Read-from-a-closed-socket-doesnt-throw-an-exception
            assertFailsWith<CancellationException> { clientByte.await() }
            assertTrue(clientSocket.isClosed)
        }
    }

    @Test
    fun socket() {
        runServer { tcp, serverSocket ->
            val listenerJob = launch {
                val serverTunnel = tunnelWithContext { currentCoroutineContext()[SocketCce]!!.socket.remoteAddress }
                while (true) {
                    val socket = serverSocket.accept()
                    launch {
                        try {
                            socket.handleRequest(ContractSerializer, serverTunnel)
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                }
            }
            try {
                val clientTunnel = ContractSerializer.tunnel { tcp.connect(serverSocket.localAddress) }
                clientTunnel.invoke()
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
                            ContractSerializer,
                            acceptorSessionFactory { connection.socket.remoteAddress }
                        )
                    }
                }
            }
            try {
                tcp.connect(serverSocket.localAddress)
                    .receiveLoop(ContractSerializer, initiatorSessionFactory())
            } finally {
                acceptorJob.cancel()
            }
        }
    }

    @Test
    fun readByteArray() = runTest {
        class Step(val size: Int, val offset: Int, val length: Int)

        suspend fun check(length: Int, vararg steps: Step) {
            var counter = 0
            val byteArray = readByteArray(length) { byteArray, offset, length ->
                val step = steps[counter++]
                assertEquals(step.size, byteArray.size)
                assertEquals(step.offset, offset)
                assertEquals(step.length, length)
            }
            assertEquals(length, byteArray.size)
            assertEquals(counter, steps.size)
        }

        check(
            10,
            Step(10, 0, 10),
        )
        check(
            1000,
            Step(1000, 0, 1000),
        )
        check(
            1001,
            Step(1000, 0, 1000),
            Step(1001, 1000, 1),
        )
        check(
            4001,
            Step(1000, 0, 1000),
            Step(2000, 1000, 1000),
            Step(4000, 2000, 2000),
            Step(4001, 4000, 1),
        )
    }
}

@file:OptIn(TestingYassApi::class)

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.TestMode
import ch.softappeal.yass2.core.TestingYassApi
import ch.softappeal.yass2.core.remote.clientTest
import ch.softappeal.yass2.core.remote.serverTunnel
import ch.softappeal.yass2.coroutines.session.ACCEPTOR
import ch.softappeal.yass2.coroutines.session.INITIATOR
import ch.softappeal.yass2.coroutines.session.sessionFactory
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun runServer(block: suspend CoroutineScope.(tcp: TcpSocketBuilder, serverSocket: ServerSocket) -> Unit) {
    runBlocking {
        SelectorManager().use { selector ->
            val tcp = aSocket(selector).tcp()
            tcp.bind(LOCAL_HOST).use { serverSocket ->
                block(tcp, serverSocket)
            }
        }
    }
}

class SocketTest {
    @OptIn(ExperimentalAtomicApi::class)
    @Test
    fun socketTest() {
        fun socketTest(testMode: TestMode) {
            runServer { tcp, serverSocket ->
                println()
                println("*** socketTest: testMode = $testMode ***")
                val address = AtomicReference<String?>(null)
                val listenerJob = launch {
                    val serverTunnel = serverTunnel("server") { _, _, invocation ->
                        // println("address: ${socket().remoteAddress}")
                        assertEquals(address.load(), socket().remoteAddress.toString())
                        invocation()
                    }
                    while (true) {
                        val clientSocket = serverSocket.accept()
                        launch {
                            try {
                                clientSocket.handleRequest(ContractSerializer, serverTunnel)
                            } catch (e: Exception) {
                                println("Exception in handleRequest: $e")
                            }
                        }
                    }
                }
                try {
                    val clientTunnel = tunnel(ContractSerializer) {
                        tcp.connect(serverSocket.localAddress).apply { address.store(localAddress.toString()) }
                    }
                    clientTunnel.clientTest(testMode, "client")
                } finally {
                    listenerJob.cancel()
                }
            }
        }
        socketTest(TestMode.Normal)
        socketTest(TestMode.Exception)
        println()
    }

    @Test
    fun socketSession() {
        fun socketSession(testMode: TestMode, initiatorRunTests: Boolean, acceptorRunTests: Boolean) {
            runServer { tcp, serverSocket ->
                println()
                println("*** socketSession: testMode = $testMode, initiatorRunTests = $initiatorRunTests, acceptorRunTests = $acceptorRunTests ***")
                val address = serverSocket.localAddress.toString()
                val acceptorJob = launch {
                    while (true) {
                        val socket = serverSocket.accept()
                        launch {
                            socket.receiveLoop(
                                ContractSerializer,
                                sessionFactory(testMode, acceptorRunTests, ACCEPTOR) {
                                    // println("address: $address")
                                    assertEquals(address, connection.socket.localAddress.toString())
                                },
                            )
                        }
                    }
                }
                try {
                    tcp
                        .connect(serverSocket.localAddress)
                        .receiveLoop(ContractSerializer, sessionFactory(testMode, initiatorRunTests, INITIATOR))
                } finally {
                    acceptorJob.cancel()
                }
            }
        }
        socketSession(TestMode.Normal, initiatorRunTests = false, acceptorRunTests = true)
        socketSession(TestMode.Normal, initiatorRunTests = true, acceptorRunTests = false)
        socketSession(TestMode.Normal, initiatorRunTests = true, acceptorRunTests = true)
        socketSession(TestMode.Exception, initiatorRunTests = false, acceptorRunTests = true)
        socketSession(TestMode.Exception, initiatorRunTests = true, acceptorRunTests = false)
        println()
    }

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

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.coroutines.session.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.random.*
import kotlin.test.*

private fun createRandomAddress() = InetSocketAddress(Host, Random.nextInt(2_000..65_000))

@Ignore // TODO
/** see [KtorReadingFromClosedSocketBugTest] */
class SocketTest {
    private val tcp = aSocket(SelectorManager(EmptyCoroutineContext)).tcp()

    @Test
    fun socket() {
        val address = createRandomAddress()
        tcp.bind(address).use { serverSocket ->
            runBlocking {
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
                    val clientTunnel = MessageTransport.socketTunnel { tcp.connect(address) }
                    clientTunnel.test(100)
                } finally {
                    listenerJob.cancel()
                }
            }
        }
    }

    @Test
    fun socketSession() {
        val address = createRandomAddress()
        tcp.bind(address).use { serverSocket ->
            runBlocking {
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
                    tcp.connect(address)
                        .receiveLoop(PacketTransport, initiatorSessionFactory(1000))
                } finally {
                    acceptorJob.cancel()
                }
            }
        }
    }
}

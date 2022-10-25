package transport.ktor

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.transport.ktor.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.test.*

private val Address = InetSocketAddress(Host, Port)

class SocketTest {
    private val tcp = aSocket(SelectorManager(EmptyCoroutineContext)).tcp()

    @Test
    fun socket() {
        runOnPlatforms(Platform.Jvm) {
            tcp.bind(Address).use { serverSocket ->
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
                        val clientTunnel = MessageTransport.socketTunnel { tcp.connect(Address) }
                        clientTunnel.test(1000)
                    } finally {
                        listenerJob.cancel()
                    }
                }
            }
        }
    }

    @Test
    fun socketSession() {
        runOnPlatforms(Platform.Jvm) {
            tcp.bind(Address).use { serverSocket ->
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
                        tcp.connect(Address)
                            .receiveLoop(PacketTransport, initiatorSessionFactory(1000))
                    } finally {
                        acceptorJob.cancel()
                    }
                }
            }
        }
    }
}

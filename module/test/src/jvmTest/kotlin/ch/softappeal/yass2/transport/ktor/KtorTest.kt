package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.net.*
import java.util.concurrent.*
import kotlin.test.*

const val Host = "localhost"
const val Port = 28947
const val Path = "/yass"
private val Address = InetSocketAddress(Host, Port)

class KtorTest {
    @AfterTest
    fun additionalWaitForServerSocketClose() {
        TimeUnit.MILLISECONDS.sleep(200)
    }

    private val tcp = aSocket(ActorSelectorManager(Dispatchers.Default)).tcp()

    @Test
    fun socket() {
        tcp.bind(Address).use { serverSocket ->
            runBlocking {
                val listenerJob = launch {
                    val serverTunnel = tunnel { currentCoroutineContext()[SocketCce]!!.socket.remoteAddress }
                    while (true) {
                        val socket = serverSocket.accept()
                        launch {
                            socket.handleRequest(MessageConfig, serverTunnel)
                        }
                    }
                }
                try {
                    val clientTunnel = MessageConfig.socketTunnel { tcp.connect(Address) }
                    clientTunnel.test(1000)
                } finally {
                    listenerJob.cancel()
                }
            }
        }
    }

    @Test
    fun socketSession() {
        tcp.bind(Address).use { serverSocket ->
            runBlocking {
                val acceptorJob = launch {
                    while (true) {
                        val socket = serverSocket.accept()
                        launch {
                            socket.receiveLoop(
                                PacketConfig,
                                acceptorSessionFactory { (connection as SocketConnection).socket.remoteAddress }
                            )
                        }
                    }
                }
                try {
                    launch {
                        tcp.connect(Address)
                            .receiveLoop(PacketConfig, initiatorSessionFactory(1000))
                    }.join()
                } finally {
                    acceptorJob.cancel()
                }
            }
        }
    }

    @Test
    fun http() {
        val engine = embeddedServer(Netty, Port) {
            routing {
                route(
                    MessageConfig,
                    Path,
                    tunnel { currentCoroutineContext()[CallCce]!!.call.request.headers[DemoHeaderKey]!! }
                )
            }
        }
        engine.start()
        try {
            runBlocking {
                HttpClient(Java).use { client ->
                    client.tunnel(MessageConfig, "http://$Host:$Port$Path") { headersOf(DemoHeaderKey, DemoHeaderValue) }
                        .test(1000)
                }
            }
        } finally {
            engine.stop(0, 0, TimeUnit.SECONDS)
        }
    }

    @Test
    fun webSocket() {
        val engine = embeddedServer(Netty, Port) {
            install(io.ktor.websocket.WebSockets)
            routing {
                webSocket(Path) {
                    receiveLoop(
                        PacketConfig,
                        acceptorSessionFactory {
                            ((connection as WebSocketConnection).session as WebSocketServerSession)
                                .call.request.headers[DemoHeaderKey]!!
                        }
                    )
                }
            }
        }
        engine.start()
        try {
            runBlocking {
                HttpClient(Java) {
                    install(io.ktor.client.features.websocket.WebSockets)
                }.use { client ->
                    client.ws(HttpMethod.Get, Host, Port, Path, { header(DemoHeaderKey, DemoHeaderValue) }) {
                        receiveLoop(PacketConfig, initiatorSessionFactory(1000))
                    }
                }
            }
        } finally {
            engine.stop(0, 0, TimeUnit.SECONDS)
        }
    }
}

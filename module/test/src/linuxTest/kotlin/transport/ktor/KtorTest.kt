@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.coroutines.session.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.test.*

const val Host = "localhost"
const val Port = 28947
const val Path = "/yass"
private val Address = InetSocketAddress(Host, Port)

@Ignore // TODO
class KtorTest {
    private val tcp = aSocket(SelectorManager(EmptyCoroutineContext)).tcp()

    @Test
    fun socket() {
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
                    clientTunnel.test(10)
                } finally {
                    listenerJob.cancel()
                }
            }
        }
    }

    @Test
    fun socketSession() {
        tcp.bind(Address).use { serverSocket ->
            try {
                runBlocking {
                    launch {
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
                    launch {
                        tcp.connect(Address)
                            .receiveLoop(PacketTransport, initiatorSessionFactory(1000))
                    }
                    delay(2_000)
                    cancel()
                }
            } catch (ignore: CancellationException) {
                ignore.printStackTrace()
            }
        }
    }

    @Test
    fun http() {
        val engine = embeddedServer(io.ktor.server.cio.CIO, Port) {
            routing {
                route(
                    MessageTransport,
                    Path,
                    tunnel { currentCoroutineContext()[CallCce]!!.call.request.headers[DemoHeaderKey]!! }
                )
            }
        }
        engine.start()
        try {
            runBlocking {
                HttpClient(io.ktor.client.engine.cio.CIO).use { client ->
                    client.tunnel(MessageTransport, "http://$Host:$Port$Path") { headersOf(DemoHeaderKey, DemoHeaderValue) }
                        .test(10)
                }
            }
        } finally {
            engine.stop()
        }
    }

    @Test
    fun webSocket() {
        val engine = embeddedServer(io.ktor.server.cio.CIO, Port) {
            install(io.ktor.server.websocket.WebSockets)
            routing {
                webSocket(Path) {
                    receiveLoop(
                        PacketTransport,
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
                HttpClient(io.ktor.client.engine.cio.CIO) {
                    install(io.ktor.client.plugins.websocket.WebSockets)
                }.use { client ->
                    client.ws(HttpMethod.Get, Host, Port, Path, { header(DemoHeaderKey, DemoHeaderValue) }) {
                        receiveLoop(PacketTransport, initiatorSessionFactory(1000))
                    }
                }
            }
        } finally {
            engine.stop()
        }
    }
}

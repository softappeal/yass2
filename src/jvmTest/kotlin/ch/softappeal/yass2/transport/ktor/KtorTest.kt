package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.session.*
import ch.softappeal.yass2.transport.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.net.*
import java.util.concurrent.*
import kotlin.coroutines.*
import kotlin.test.*

val Config = TransportConfig(GeneratedSerializer, 100, 100)
const val Host = "localhost"
const val Port = 28947
const val Path = "/yass"
private val Address = InetSocketAddress(Host, Port)

@KtorExperimentalAPI
class KtorTest {
    @AfterTest
    fun additionalWaitForServerSocketClose() {
        TimeUnit.MILLISECONDS.sleep(200)
    }

    private val tcp = aSocket(ActorSelectorManager(Dispatchers.Default)).tcp()

    @Test
    fun socket() {
        suspend fun serverContext() = coroutineContext[SocketCce]?.socket?.remoteAddress!!
        tcp.bind(Address).use { serverSocket ->
            runBlocking {
                val listenerJob = launch {
                    while (true) {
                        serverSocket.accept()
                            .handleRequest(Config, tunnel(::serverContext))
                    }
                }
                try {
                    val socketConnector: SocketConnector = { tcp.connect(Address) }
                    val clientTunnel = Config.socketTunnel(socketConnector)
                    clientTunnel.test(1000)
                } finally {
                    listenerJob.cancel()
                }
            }
        }
    }

    @Test
    fun socketSession() {
        fun Session.acceptorContext() = (connection as SocketConnection).socket.remoteAddress
        tcp.bind(Address).use { serverSocket ->
            runBlocking {
                val acceptorJob = launch {
                    while (true) {
                        serverSocket.accept()
                            .receiveLoop(Config, acceptorSessionFactory { acceptorContext() })
                    }
                }
                try {
                    launch {
                        tcp.connect(Address)
                            .receiveLoop(Config, initiatorSessionFactory(1000))
                    }.join()
                } finally {
                    acceptorJob.cancel()
                }
            }
        }
    }

    @Test
    fun http() {
        suspend fun serverContext() = coroutineContext[CallCce]?.call?.request?.uri!!
        val engine = embeddedServer(io.ktor.server.cio.CIO, Port) {
            routing {
                route(Config, Path, tunnel(::serverContext))
            }
        }
        engine.start()
        try {
            runBlocking {
                // note: CIO seems not to support keep-alive, however io.ktor.client.engine.apache.Apache does
                HttpClient(io.ktor.client.engine.cio.CIO).use { client ->
                    client.tunnel(Config, "http://$Host:$Port$Path")
                        .test(1000)
                }
            }
        } finally {
            engine.stop(0, 0, TimeUnit.SECONDS)
        }
    }

    @Test
    fun webSocket() {
        fun Session.acceptorContext() = ((connection as WebSocketConnection).session as WebSocketServerSession).call.request.uri
        val engine = embeddedServer(io.ktor.server.cio.CIO, Port) {
            install(io.ktor.websocket.WebSockets)
            routing {
                webSocket(Path) {
                    receiveLoop(Config, acceptorSessionFactory { acceptorContext() })
                }
            }
        }
        engine.start()
        try {
            runBlocking {
                HttpClient {
                    install(io.ktor.client.features.websocket.WebSockets)
                }.use { client ->
                    client.ws(HttpMethod.Get, Host, Port, Path) { receiveLoop(Config, initiatorSessionFactory(1000)) }
                }
            }
        } finally {
            engine.stop(0, 0, TimeUnit.SECONDS)
        }
    }
}

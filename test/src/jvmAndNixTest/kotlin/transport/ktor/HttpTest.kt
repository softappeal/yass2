@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.Calculator
import ch.softappeal.yass2.contract.CalculatorId
import ch.softappeal.yass2.contract.DEMO_HEADER_KEY
import ch.softappeal.yass2.contract.DEMO_HEADER_VALUE
import ch.softappeal.yass2.contract.MessageSerializer
import ch.softappeal.yass2.contract.MessageTransport
import ch.softappeal.yass2.contract.PacketTransport
import ch.softappeal.yass2.contract.reflect.proxy
import ch.softappeal.yass2.contract.reflect.service
import ch.softappeal.yass2.remote.coroutines.session.acceptorSessionFactory
import ch.softappeal.yass2.remote.coroutines.session.initiatorSessionFactory
import ch.softappeal.yass2.remote.coroutines.session.test
import ch.softappeal.yass2.remote.coroutines.session.tunnel
import ch.softappeal.yass2.remote.tunnel
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.StringEncoder
import ch.softappeal.yass2.transport.ContextMessageSerializer
import ch.softappeal.yass2.transport.Transport
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.header
import io.ktor.http.headersOf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.utils.io.core.use
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

const val LOCAL_HOST = "localhost"
const val PATH = "/yass"

private fun Application.httpModule() {
    routing {
        route(
            MessageTransport,
            PATH,
            tunnel { currentCoroutineContext()[CallCce]!!.call.request.headers[DEMO_HEADER_KEY]!! }
        )
    }
}

private fun Application.webSocketModule() {
    install(io.ktor.server.websocket.WebSockets)
    routing {
        webSocket(PATH) {
            receiveLoop(
                PacketTransport,
                acceptorSessionFactory { (connection.session as WebSocketServerSession).call.request.headers[DEMO_HEADER_KEY]!! }
            )
        }
    }
}

class HttpTest {
    @Test
    fun http() {
        val server = embeddedServer(io.ktor.server.cio.CIO, 0, module = Application::httpModule)
        server.start()
        try {
            runBlocking {
                val randomPort = server.engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO).use { client ->
                    client
                        .tunnel(MessageTransport, "http://$LOCAL_HOST:$randomPort$PATH") {
                            headersOf(DEMO_HEADER_KEY, DEMO_HEADER_VALUE)
                        }
                        .test(100)
                }
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun webSocket() {
        val server = embeddedServer(io.ktor.server.cio.CIO, 0, module = Application::webSocketModule)
        server.start()
        try {
            runBlocking {
                val randomPort = server.engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO) {
                    install(io.ktor.client.plugins.websocket.WebSockets)
                }.use { client ->
                    client.ws("ws://$LOCAL_HOST:$randomPort$PATH", { header(DEMO_HEADER_KEY, DEMO_HEADER_VALUE) }) {
                        receiveLoop(PacketTransport, initiatorSessionFactory(1000))
                    }
                }
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun context() {
        var context: String? = null
        val transport = Transport(
            ContextMessageSerializer(
                object : BinarySerializer() {
                    init {
                        initialize(StringEncoder())
                    }
                },
                MessageSerializer,
                { context },
                { context = it },
            ),
            100, 100,
        )
        val server = embeddedServer(io.ktor.server.cio.CIO, 0) {
            routing {
                val calculator = object : Calculator {
                    override suspend fun add(a: Int, b: Int): Int {
                        assertEquals("client", context)
                        context = "server"
                        return a + b
                    }

                    override suspend fun divide(a: Int, b: Int): Int = error("not needed")
                }
                route(transport, PATH, tunnel(CalculatorId.service(calculator)))
            }
        }
        server.start()
        try {
            runBlocking {
                val randomPort = server.engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO).use { client ->
                    val clientTunnel = client.tunnel(transport, "http://$LOCAL_HOST:$randomPort$PATH")
                    val calculator = CalculatorId.proxy(clientTunnel)
                    context = "client"
                    assertEquals(5, calculator.add(2, 3))
                    assertEquals("server", context)
                }
            }
        } finally {
            server.stop()
        }
    }
}

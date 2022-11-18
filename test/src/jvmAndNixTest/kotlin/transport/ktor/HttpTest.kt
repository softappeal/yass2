@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
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
import kotlin.time.*

const val Host = "localhost"
const val Path = "/yass"

class HttpTest {
    @Test
    fun http() {
        val engine = embeddedServer(io.ktor.server.cio.CIO, 0) {
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
                val randomPort = engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO).use { client ->
                    client.tunnel(MessageTransport, "http://$Host:$randomPort$Path") { headersOf(DemoHeaderKey, DemoHeaderValue) }
                        .test(100)
                }
            }
        } finally {
            engine.stop()
        }
    }

    @Test
    fun webSocket() {
        val engine = embeddedServer(io.ktor.server.cio.CIO, 0) {
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
                val randomPort = engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO) {
                    install(io.ktor.client.plugins.websocket.WebSockets)
                }.use { client ->
                    client.ws(HttpMethod.Get, Host, randomPort, Path, { header(DemoHeaderKey, DemoHeaderValue) }) {
                        receiveLoop(PacketTransport, initiatorSessionFactory(1000))
                    }
                }
            }
        } finally {
            engine.stop()
        }
    }

    @Test
    fun context() {
        var context: String? = null
        val transport = Transport(
            ContextMessageSerializer(
                BinarySerializer(listOf(StringEncoder)), MessageSerializer,
                { context }, { context = it },
            ),
            100, 100,
        )
        val engine = embeddedServer(io.ktor.server.cio.CIO, 0) {
            routing {
                val calculator = object : Calculator {
                    override suspend fun add(a: Int, b: Int): Int {
                        assertEquals("client", context)
                        context = "server"
                        return a + b
                    }

                    override suspend fun divide(a: Int, b: Int): Int = error("not needed")
                }
                route(transport, Path, ::generatedInvoke.tunnel(listOf(CalculatorId(calculator))))
            }
        }
        engine.start()
        try {
            runBlocking {
                val randomPort = engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO).use { client ->
                    val clientTunnel = client.tunnel(transport, "http://$Host:$randomPort$Path")
                    val calculator = generatedRemoteProxyFactory(clientTunnel)(CalculatorId)
                    context = "client"
                    assertEquals(5, calculator.add(2, 3))
                    assertEquals("server", context)
                }
            }
        } finally {
            engine.stop()
        }
    }
}

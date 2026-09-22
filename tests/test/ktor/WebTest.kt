package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.TestMode
import ch.softappeal.yass2.core.remote.clientTest
import ch.softappeal.yass2.core.remote.serverTunnel
import ch.softappeal.yass2.coroutines.session.ACCEPTOR
import ch.softappeal.yass2.coroutines.session.INITIATOR
import ch.softappeal.yass2.coroutines.session.sessionFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val LOCAL_HOST = "localhost"
private const val BROWSER_PORT = 28948
private const val PATH = "/yass"

private const val CONTEXT_HEADER = "Context-Header"
private const val CONTEXT_VALUE = "ContextValue"

suspend fun webClientTest(port: Int = BROWSER_PORT, httpClientEngineFactory: HttpClientEngineFactory<*>) {
    HttpClient(httpClientEngineFactory) {
        install(Plugin)
    }.use { client ->
        var counter = 0
        var doIntercept = true

        client.plugin(HttpSend).intercept { request ->
            if (!doIntercept) execute(request) else {
                counter++
                request.headers.append(CONTEXT_HEADER, "$CONTEXT_VALUE-$counter")
                execute(request).apply {
                    val context = response.headers[CONTEXT_HEADER]!!
                    assertEquals(CONTEXT_VALUE, context)
                }
            }
        }

        doIntercept = true
        suspend fun http(testMode: TestMode) {
            println()
            println("*** http: testMode = $testMode ***")
            @Suppress("HttpUrlsUsage") val tunnel = client.tunnel("http://$LOCAL_HOST:$port$PATH", ContractSerializer)
            tunnel.clientTest(testMode, "client")
        }
        http(TestMode.Normal)
        http(TestMode.Exception)

        doIntercept = false
        suspend fun webSocket(testMode: TestMode) {
            println()
            println("*** webSocket: testMode = $testMode ***")
            client.ws(
                "ws://$LOCAL_HOST:$port$PATH",
                { header(CONTEXT_HEADER, CONTEXT_VALUE) }, // header is not set if run in browser
            ) {
                receiveLoop(ContractSerializer, sessionFactory(testMode, runTests = true, INITIATOR))
            }
        }
        webSocket(TestMode.Normal)
        webSocket(TestMode.Exception)

        println()
    }
}

@Suppress("HttpUrlsUsage")
fun createWebServer(port: Int = BROWSER_PORT, additionalRouting: Routing.() -> Unit = {}) =
    embeddedServer(io.ktor.server.cio.CIO, port) {
        println("http://$LOCAL_HOST:$port/wasm/")
        println("http://$LOCAL_HOST:$port/js/")

        install(WebSockets)
        install(StatusPages) {
            exception<Exception> { call, cause ->
                println("server.exception: $cause")
                call.respond(HttpStatusCode.InternalServerError, "InternalServerError")
            }
        }
        routing {
            route(PATH, ContractSerializer, serverTunnel("server") { _, _, invocation ->
                val call = call()
                val context = call.request.headers[CONTEXT_HEADER]!!
                // println("context: $context")
                assertTrue(context.startsWith("$CONTEXT_VALUE-"))
                try {
                    invocation()
                } finally {
                    call.response.headers.append(CONTEXT_HEADER, CONTEXT_VALUE)
                }
            })
            webSocket(PATH) {
                receiveLoop(ContractSerializer, sessionFactory(TestMode.Normal, runTests = false, ACCEPTOR) {
                    val context = (connection.session as WebSocketServerSession).call.request.headers[CONTEXT_HEADER]
                    // println("context: $context")
                    if (context != null) assertEquals(CONTEXT_VALUE, context)
                })
            }
            additionalRouting()
        }
    }

class WebTest {
    @Test
    fun test() = runTest {
        val port = Random.nextInt(2_000, 30_000)
        val server = createWebServer(port)
        server.startSuspend()
        try {
            withContext(Dispatchers.Default.limitedParallelism(1)) {
                webClientTest(port, io.ktor.client.engine.cio.CIO)
            }
        } finally {
            server.stopSuspend()
        }
    }
}

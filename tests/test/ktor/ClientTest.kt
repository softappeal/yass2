package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.TestMode
import ch.softappeal.yass2.core.remote.clientTest
import ch.softappeal.yass2.coroutines.session.INITIATOR
import ch.softappeal.yass2.coroutines.session.sessionFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.header
import kotlin.test.assertEquals

const val LOCAL_HOST = "localhost"
const val PORT = 28948
const val PATH = "/yass"

const val CONTEXT_HEADER = "Context-Header"
const val CONTEXT_VALUE = "ContextValue"

suspend fun clientTest(httpClientEngineFactory: HttpClientEngineFactory<*>) {
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
            @Suppress("HttpUrlsUsage") val tunnel = client.tunnel("http://$LOCAL_HOST:$PORT$PATH", ContractSerializer)
            tunnel.clientTest(testMode, "client")
        }
        http(TestMode.Normal)
        http(TestMode.Exception)

        doIntercept = false
        suspend fun webSocket(testMode: TestMode) {
            println()
            println("*** webSocket: testMode = $testMode ***")
            client.ws(
                "ws://$LOCAL_HOST:$PORT$PATH",
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

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.remote.clientTest
import ch.softappeal.yass2.coroutines.session.initiatorSessionFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.header
import kotlin.test.assertEquals

const val LOCAL_HOST = "localhost"
const val PORT = 28947
const val PATH = "/yass"

const val CONTEXT_HEADER = "Context-Header"
const val CONTEXT_VALUE = "ContextValue"

suspend fun clientTest(httpClientEngineFactory: HttpClientEngineFactory<*>) {
    @Suppress("HttpUrlsUsage")
    HttpClient(httpClientEngineFactory) {
        install(Plugin)
    }.use { client ->
        var counter = 0
        client.tunnel("http://$LOCAL_HOST:$PORT$PATH", ContractSerializer).clientTest { _, _, invocation ->
            counter++
            buildRequest({ headers.append(CONTEXT_HEADER, "$CONTEXT_VALUE-$counter") }) {
                handleResponse({
                    val context = headers[CONTEXT_HEADER]!!
                    println("response:<$context>")
                    assertEquals(CONTEXT_VALUE, context)
                }) {
                    invocation()
                }
            }
        }
        client.ws(
            "ws://$LOCAL_HOST:$PORT$PATH",
            { header(CONTEXT_HEADER, CONTEXT_VALUE) },
        ) { receiveLoop(ContractSerializer, initiatorSessionFactory()) }
    }
}

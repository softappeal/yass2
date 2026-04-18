package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.remote.clientTest
import ch.softappeal.yass2.coroutines.session.initiatorSessionFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.HttpRequestBuilder

const val LOCAL_HOST = "localhost"
const val PORT = 28947
const val PATH = "/yass"

const val CONTEXT_HEADER = "context"

suspend fun clientTest(httpClientEngineFactory: HttpClientEngineFactory<*>) {
    @Suppress("HttpUrlsUsage")
    HttpClient(httpClientEngineFactory) {
        install(Plugin)
    }.use { client ->
        var counter = 0
        val requestBuilder: HttpRequestBuilder.() -> Unit = {
            headers.append(CONTEXT_HEADER, counter.toString())
            counter++
        }
        /*
        val contextInterceptor: Interceptor = { _, _, invoke ->
            counter++
            invoke()
        }
        */
        client.tunnel(ContractSerializer, "http://$LOCAL_HOST:$PORT$PATH", requestBuilder).clientTest()
        client.ws("ws://$LOCAL_HOST:$PORT$PATH") { receiveLoop(ContractSerializer, initiatorSessionFactory()) }
    }
}

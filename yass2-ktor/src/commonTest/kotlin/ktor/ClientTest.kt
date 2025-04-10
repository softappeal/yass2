package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.core.contract.ContractTransport
import ch.softappeal.yass2.core.remote.test
import ch.softappeal.yass2.coroutines.session.initiatorSessionFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.header
import io.ktor.http.headersOf

const val DEMO_HEADER_KEY = "Demo-Header-Key"
const val DEMO_HEADER_VALUE = "Demo-Header-Value"

const val LOCAL_HOST = "localhost"
const val PORT = 28947
const val PATH = "/yass"

suspend fun clientTest(httpClientEngineFactory: HttpClientEngineFactory<*>) {
    @Suppress("HttpUrlsUsage")
    HttpClient(httpClientEngineFactory) {
        install(Plugin)
    }.use { client ->
        client.tunnel(ContractTransport, "http://$LOCAL_HOST:$PORT$PATH") {
            headersOf(DEMO_HEADER_KEY, DEMO_HEADER_VALUE)
        }.test()
        client.ws("ws://$LOCAL_HOST:$PORT$PATH", {
            header(DEMO_HEADER_KEY, DEMO_HEADER_VALUE)
        }) {
            receiveLoop(ContractTransport, initiatorSessionFactory())
        }
    }
}

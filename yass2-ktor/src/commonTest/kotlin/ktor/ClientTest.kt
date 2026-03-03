package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.remote.invoke
import ch.softappeal.yass2.coroutines.session.initiatorSessionFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws

const val LOCAL_HOST = "localhost"
const val PORT = 28947
const val PATH = "/yass"

suspend fun clientTest(httpClientEngineFactory: HttpClientEngineFactory<*>) {
    @Suppress("HttpUrlsUsage")
    HttpClient(httpClientEngineFactory) {
        install(Plugin)
    }.use { client ->
        client.tunnel(ContractSerializer, "http://$LOCAL_HOST:$PORT$PATH").invoke()
        client.ws("ws://$LOCAL_HOST:$PORT$PATH") { receiveLoop(ContractSerializer, initiatorSessionFactory()) }
    }
}

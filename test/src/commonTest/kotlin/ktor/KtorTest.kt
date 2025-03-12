package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.coroutines.initiatorSessionFactory
import ch.softappeal.yass2.coroutines.test
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val LOCAL_HOST = "localhost"
const val PORT = 28947
const val PATH = "/yass"

fun ktorClientTest(httpClientEngineFactory: HttpClientEngineFactory<*>) {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        HttpClient(httpClientEngineFactory) {
            install(Plugin)
        }.use { client ->
            client.tunnel(ContractTransport, PATH).test(1000)
            client.ws("ws://$LOCAL_HOST:$PORT$PATH") {
                receiveLoop(ContractTransport, initiatorSessionFactory(1000))
            }
        }
    }
}

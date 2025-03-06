package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.contract.PATH
import ch.softappeal.yass2.contract.WS_URL
import ch.softappeal.yass2.coroutines.initiatorSessionFactory
import ch.softappeal.yass2.coroutines.test
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        HttpClient(JsClient()) {
            install(Plugin)
        }.use { client ->
            client.tunnel(ContractTransport, PATH).test(1000)
            client.ws(WS_URL) {
                receiveLoop(ContractTransport, initiatorSessionFactory(1000))
            }
        }
    }
}

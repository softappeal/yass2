package ch.softappeal.yass2.js

import ch.softappeal.yass2.contract.MessageTransport
import ch.softappeal.yass2.contract.PacketTransport
import ch.softappeal.yass2.ktor.receiveLoop
import ch.softappeal.yass2.ktor.tunnel
import ch.softappeal.yass2.remote.coroutines.initiatorSessionFactory
import ch.softappeal.yass2.remote.coroutines.test
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import io.ktor.utils.io.core.use
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val HttpUrl = "/yass"
private const val WsUrl = "ws://localhost:28947/yass"

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        HttpClient(JsClient()) {
            install(Plugin)
        }.use { client ->
            client.tunnel(MessageTransport, HttpUrl).test(1000)
            client.ws(WsUrl) {
                receiveLoop(PacketTransport, initiatorSessionFactory(1000))
            }
        }
    }
}

package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.contract.MessageTransport
import ch.softappeal.yass2.contract.PacketTransport
import ch.softappeal.yass2.remote.coroutines.session.initiatorSessionFactory
import ch.softappeal.yass2.remote.coroutines.session.test
import ch.softappeal.yass2.transport.ktor.receiveLoop
import ch.softappeal.yass2.transport.ktor.tunnel
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient
import io.ktor.client.plugins.websocket.WebSockets.Plugin
import io.ktor.client.plugins.websocket.ws
import io.ktor.utils.io.core.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        val httpUrl = "/yass"
        val wsUrl = "ws://localhost:28947/yass"
        if (false) {
            MessageTransport.tunnel(httpUrl).test(1000)
            PacketTransport.connect(wsUrl, CoroutineScope(Job()).initiatorSessionFactory(1000))
        } else {
            HttpClient(JsClient()) {
                install(Plugin)
            }.use { client ->
                client.tunnel(MessageTransport, httpUrl).test(1000)
                client.ws(wsUrl) {
                    receiveLoop(PacketTransport, initiatorSessionFactory(1000))
                }
            }
        }
    }
}

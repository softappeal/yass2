package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.ktor.receiveLoop
import ch.softappeal.yass2.ktor.tunnel
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.JsClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.ws
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalJsExport::class) @JsExport
public fun showJsUsage() {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        showUsage()

        println("*** useRemoting ***")

        HttpClient(JsClient()) {
            install(WebSockets)
        }.use { client ->

            // shows client-side unidirectional remoting with Http
            useServices(client.tunnel(ContractTransport, "/yass"))

            // shows client-side session based bidirectional remoting with WebSocket
            client.ws("ws://localhost:28947/yass") {
                receiveLoop(ContractTransport, CoroutineScope(Job()).initiatorSessionFactory())
            }

        }
    }
}

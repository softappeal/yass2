package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.transport.js.connect
import ch.softappeal.yass2.transport.js.tunnel
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

        // shows client-side unidirectional remoting with Http
        useServices(MessageTransport.tunnel("/yass"))

        // shows client-side session based bidirectional remoting with WebSocket
        PacketTransport.connect("ws://localhost:28947/yass", CoroutineScope(Job()).initiatorSessionFactory())
    }
}

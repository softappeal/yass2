package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.coroutines.session.*
import kotlinx.coroutines.*

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        MessageTransport.tunnel("/yass").test(1000)
        val initiatorSessionFactory = CoroutineScope(Job()).initiatorSessionFactory(1000)
        PacketTransport.connect("ws://localhost:28947/yass", initiatorSessionFactory)
    }
}

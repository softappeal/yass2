package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.contract.MessageTransport
import ch.softappeal.yass2.contract.PacketTransport
import ch.softappeal.yass2.remote.coroutines.session.initiatorSessionFactory
import ch.softappeal.yass2.remote.coroutines.session.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        MessageTransport.tunnel("/yass").test(1000)
        PacketTransport.connect("ws://localhost:28947/yass", CoroutineScope(Job()).initiatorSessionFactory(1000))
    }
}

package ch.softappeal.yass2.ktor

import io.ktor.client.engine.js.JsClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        ktorClientTest(JsClient())
    }
}

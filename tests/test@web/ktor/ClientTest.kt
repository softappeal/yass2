package ch.softappeal.yass2.ktor

import io.ktor.client.engine.js.JsClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalJsExport::class) @JsExport
fun clientTest() {
    MainScope().launch {
        clientTest(JsClient())
    }
}

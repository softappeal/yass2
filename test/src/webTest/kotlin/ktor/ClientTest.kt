package ch.softappeal.yass2.ktor

import io.ktor.client.engine.js.JsClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class) @JsExport
fun clientTest() {
    MainScope().launch {
        clientTest(JsClient())
    }
}

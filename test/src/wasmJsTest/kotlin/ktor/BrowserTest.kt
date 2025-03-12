package ch.softappeal.yass2.ktor

import io.ktor.client.engine.js.JsClient

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    ktorClientTest(JsClient())
}

package ch.softappeal.yass2.ktor

import io.ktor.client.engine.js.JsClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test

@OptIn(ExperimentalJsExport::class) @JsExport
fun remoteTest() {
    MainScope().launch {
        ktorClientTest(JsClient())
    }
}

class BrowserTest {
    @Ignore
    @Test
    fun test() = runTest {
        ktorClientTest(JsClient())
    }
}

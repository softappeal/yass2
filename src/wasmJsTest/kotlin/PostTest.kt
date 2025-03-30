package demo

import io.ktor.client.engine.js.JsClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test

@OptIn(ExperimentalJsExport::class) @JsExport
fun postRequest() {
    MainScope().launch {
        postRequest(JsClient())
    }
}

class PostTest {
    @Ignore
    @Test
    fun test() = runTest {
        postRequest(JsClient())
    }
}

package ch.softappeal.yass2

import kotlinx.coroutines.*

@PublishedApi
internal const val CurrentMs = "new Date().getTime()"

actual inline fun yassMeasureTimeMillis(block: () -> Unit): Long {
    val start = js(CurrentMs) as Double
    block()
    return (js(CurrentMs) as Double - start).toLong()
}

actual fun yassRunBlocking(block: suspend CoroutineScope.() -> Unit) {
    GlobalScope.launch { block() }
}

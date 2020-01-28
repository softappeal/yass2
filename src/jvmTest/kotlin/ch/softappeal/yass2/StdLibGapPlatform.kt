package ch.softappeal.yass2

import kotlinx.coroutines.*
import kotlin.system.*

actual inline fun yassMeasureTimeMillis(block: () -> Unit): Long = measureTimeMillis(block)

actual fun yassRunBlocking(block: suspend CoroutineScope.() -> Unit) {
    runBlocking { block() }
}

package ch.softappeal.yass2

import kotlinx.coroutines.*

expect inline fun yassMeasureTimeMillis(block: () -> Unit): Long

expect fun yassRunBlocking(block: suspend CoroutineScope.() -> Unit)

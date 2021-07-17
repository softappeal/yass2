package ch.softappeal.yass2

import kotlinx.coroutines.*

actual fun yassRunBlocking(block: suspend CoroutineScope.() -> Unit) {
    runBlocking { block() }
}

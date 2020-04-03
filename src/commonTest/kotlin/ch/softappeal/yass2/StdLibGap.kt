package ch.softappeal.yass2

import kotlinx.coroutines.*

expect fun yassRunBlocking(block: suspend CoroutineScope.() -> Unit)

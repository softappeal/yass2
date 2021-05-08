package ch.softappeal.yass2

import kotlinx.coroutines.*

actual fun yassRunBlocking(block: suspend CoroutineScope.() -> Unit) {
    @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        block()
        println("<<< after yassRunBlocking >>>")
    }
}

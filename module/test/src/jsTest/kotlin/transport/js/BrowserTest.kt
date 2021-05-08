package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import kotlinx.coroutines.*
import kotlin.js.Promise

@Suppress("unused")
@JsName("callSuspendFunction")
fun <T> callSuspendFunction(block: suspend () -> T): Promise<T> = @OptIn(DelicateCoroutinesApi::class) GlobalScope.promise { block() }

@Suppress("unused")
suspend fun remoteTest(): String {
    MessageConfig.tunnel("/yass").test(1000)
    val initiatorSessionFactory = CoroutineScope(Job()).initiatorSessionFactory(1000)
    PacketConfig.connect("ws://localhost:28947/yass", initiatorSessionFactory)
    return "done"
}

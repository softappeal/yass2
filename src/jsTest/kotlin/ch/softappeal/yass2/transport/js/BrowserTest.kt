package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.transport.*
import kotlinx.coroutines.*
import kotlin.js.Promise

@Suppress("unused")
@JsName("callSuspendFunction")
fun <T> callSuspendFunction(block: suspend () -> T): Promise<T> = GlobalScope.promise { block() }

private val Config = TransportConfig(GeneratedSerializer, 100)

@Suppress("unused")
suspend fun remoteTest(): String {
    Config.tunnel("/yass").test(1000)
    val initiatorSessionFactory = CoroutineScope(Job()).initiatorSessionFactory(1000)
    Config.connect("ws://localhost:28947/yass", initiatorSessionFactory)
    return "done"
}

@file:Suppress("unused")

package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.transport.js.*
import ch.softappeal.yass2.tutorial.contract.*
import ch.softappeal.yass2.tutorial.contract.generated.*
import kotlinx.coroutines.*
import kotlin.js.Promise

@JsName("callSuspendFunction")
fun <T> callSuspendFunction(block: suspend () -> T): Promise<T> = @OptIn(DelicateCoroutinesApi::class) GlobalScope.promise { block() }

suspend fun showUsage(): String {
    showGeneratedUsage()

    println("*** useRemoting ***")

    // shows client-side unidirectional remoting with Http
    useServices(MessageTransport.tunnel("/yass"), ::remoteProxyFactory)

    // shows client-side session based bidirectional remoting with WebSocket
    val initiatorSessionFactory = CoroutineScope(Job()).initiatorSessionFactory()
    PacketTransport.connect("ws://localhost:28947/yass", initiatorSessionFactory)

    return "done"
}

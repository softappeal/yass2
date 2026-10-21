package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.remote.Interceptor
import ch.softappeal.yass2.remote.PassThroughInterceptor
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.remote.plus
import ch.softappeal.yass2.remote.tunnel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

suspend fun Tunnel.clientTest(testMode: TestMode, interceptor: String) {
    CalculatorId.proxy(this).proxy(printer(interceptor)).interceptorTest(testMode)
}

fun serverTunnel(interceptor: String, context: Interceptor) = tunnel(
    CalculatorId.service(CalculatorImpl.proxy(printer(interceptor) + context))
)

class RemoteTest {
    @Test
    fun clientTest() = runTest {
        val tunnel = serverTunnel("server", PassThroughInterceptor)
        tunnel.clientTest(TestMode.Normal, "client")
        tunnel.clientTest(TestMode.Exception, "client")
    }
}

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.remote.Interceptor
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.remote.plus

suspend fun Tunnel.clientTest(testMode: TestMode, interceptor: String) {
    CalculatorId.proxy(this).proxy(printer(interceptor)).clientTest(testMode)
}

fun serverTunnel(interceptor: String, context: Interceptor) = Tunnel(
    CalculatorId.Service(CalculatorImpl.proxy(printer(interceptor) + context))
)

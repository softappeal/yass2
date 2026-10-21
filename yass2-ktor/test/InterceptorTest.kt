package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.remote.Interceptor
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

object CalculatorImpl : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun noParametersNoResult() {}
    override suspend fun delay(milliSeconds: Int) = kotlinx.coroutines.delay(milliSeconds.milliseconds)
    override suspend fun divide(a: Int, b: Int): Int {
        if (b == 0) throw DivideByZeroException()
        if (b == 999) error("simulates technical exception in service implementation")
        return a / b
    }
}

enum class TestMode { Normal, Exception }

suspend fun Calculator.clientTest(testMode: TestMode) {
    if (testMode == TestMode.Exception) {
        assertFails { divide(0, 999) }
        return
    }

    assertEquals(5, add(2, 3))
    noParametersNoResult()
    assertFailsWith<DivideByZeroException> { divide(12, 0) }
    withTimeout(200.milliseconds) { delay(100) }
    assertFailsWith<TimeoutCancellationException> { withTimeout(100.milliseconds) { delay(200) } }
    kotlinx.coroutines.delay(200.milliseconds) // to make sure that the server side delay is finished
}

fun printer(name: String): Interceptor = { function, parameters, invocation ->
    println("$name: ${function.name} $parameters")
    try {
        invocation().apply { println("$name: $this") }
    } catch (e: Exception) {
        println("$name: $e")
        throw e
    }
}

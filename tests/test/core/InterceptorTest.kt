package ch.softappeal.yass2.core

import ch.softappeal.yass2.Calculator
import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.proxy
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
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

suspend fun Calculator.interceptorTest(testMode: TestMode) {
    if (testMode == TestMode.Exception) {
        assertFails { divide(0, 999) }
        return
    }

    var functionName: String? = null
    var params: List<Any?>? = null
    val calculator = proxy { function, parameters, invocation ->
        functionName = function
        params = parameters
        invocation()
    }

    assertEquals(5, calculator.add(2, 3))
    assertEquals("add", functionName)
    assertEquals(listOf(2, 3), params)

    calculator.noParametersNoResult()
    assertEquals("noParametersNoResult", functionName)
    assertEquals(listOf(), params)

    assertFailsWith<DivideByZeroException> { calculator.divide(12, 0) }

    withTimeout(200.milliseconds) { calculator.delay(100) }

    assertFailsWith<TimeoutCancellationException> {
        withTimeout(100.milliseconds) { calculator.delay(200) }
    }
    kotlinx.coroutines.delay(200.milliseconds) // to make sure that the server side delay is finished
}

fun printer(name: String): Interceptor = { function, parameters, invocation ->
    println("$name: $function $parameters")
    try {
        invocation().apply { println("$name: $this") }
    } catch (e: Exception) {
        println("$name: $e")
        throw e
    }
}

class InterceptorTest {
    @Test
    fun plus() = runTest {
        val result = "result"
        val function = "add"
        val parameters = listOf(1, 2, 3)
        fun interceptor(check: () -> Unit): Interceptor = { f, p, invocation ->
            assertSame(function, f)
            assertSame(parameters, p)
            check()
            invocation().apply { assertSame(result, this) }
        }

        var value1: Int? = null
        var value2: Int? = null
        val interceptor1 = interceptor {
            assertNull(value1)
            assertNull(value2)
            value1 = 1
        }
        val interceptor2 = interceptor {
            assertEquals(1, value1)
            assertNull(value2)
            value2 = 2
        }
        assertSame(result, (interceptor1 + interceptor2)(function, parameters) { result })
        assertEquals(1, value1)
        assertEquals(2, value2)
    }

    @Test
    fun passThroughInterceptor() = runTest {
        assertEquals(5, PassThroughInterceptor("x", listOf()) { 5 })
    }

    @Test
    fun interceptorTest() = runTest {
        val calculator = CalculatorImpl.proxy(printer("interceptor"))
        calculator.interceptorTest(TestMode.Normal)
        calculator.interceptorTest(TestMode.Exception)
    }
}

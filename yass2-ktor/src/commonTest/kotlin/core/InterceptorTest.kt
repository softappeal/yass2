package ch.softappeal.yass2.core

import ch.softappeal.yass2.Calculator
import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.Echo
import ch.softappeal.yass2.proxy
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds

object CalculatorImpl : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

object EchoImpl : Echo {
    override suspend fun echo(value: Any?) = value
    override suspend fun echoRequired(value: Any) = value
    override suspend fun noParametersNoResult() {}
    override suspend fun delay(milliSeconds: Int) = kotlinx.coroutines.delay(milliSeconds.toLong())
    override suspend fun echoException(value: Exception) = value

    @Suppress("SameReturnValue")
    override suspend fun echoMonster(a: List<*>, b: List<List<String?>?>, c: Map<out Int, String>, d: Pair<*, *>) = null
}

val Printer: Interceptor = { function, parameters, invoke ->
    println("$function $parameters -> ")
    try {
        val result = invoke()
        println("-> $result")
        result
    } catch (e: Exception) {
        println("-> $e")
        throw e
    }
}

suspend fun invoke(calculatorImpl: Calculator, echoImpl: Echo) {
    var counter = 0
    var functionName: String? = null
    var params: List<Any?>? = null
    var result: Any? = null
    val testInterceptor: Interceptor = { function, parameters, invoke ->
        counter++
        functionName = function
        params = parameters
        result = invoke()
        result
    }
    val interceptor = testInterceptor + Printer
    val calculator = calculatorImpl.proxy(interceptor)
    val echo = echoImpl.proxy(interceptor)

    assertEquals(5, calculator.add(2, 3))
    assertEquals("add", functionName)
    assertEquals(listOf(2, 3), params)
    assertEquals(5, result)
    assertEquals(1, counter)

    assertEquals(3, calculator.divide(12, 4))
    assertEquals(2, counter)
    assertFailsWith<DivideByZeroException> { calculator.divide(12, 0) }
    assertEquals(3, counter)

    echo.noParametersNoResult()
    assertEquals("hello", echo.echo("hello"))
    assertEquals(3, (echo.echo(ByteArray(3)) as ByteArray).size)

    withTimeout(200.milliseconds) { echo.delay(100) }
    assertFailsWith<TimeoutCancellationException> {
        withTimeout(100.milliseconds) { echo.delay(200) }
    }

    println("done")
}

class InterceptorTest {
    @Test
    fun plus() = runTest {
        val result = "result"
        val function = "add"
        val parameters = listOf(1, 2, 3)
        fun interceptor(check: () -> Unit): Interceptor = { f, p, invoke ->
            assertSame(function, f)
            assertSame(parameters, p)
            check()
            invoke().apply { assertSame(result, this) }
        }

        var value1: Int? = null
        var value2: Int? = null
        val interceptor1 = interceptor {
            assertNull(value1)
            assertNull(value2)
            value1 = 1
            println("interceptor1")
        }
        val interceptor2 = interceptor {
            assertEquals(1, value1)
            assertNull(value2)
            value2 = 2
            println("interceptor2")
        }
        assertSame(result, (interceptor1 + interceptor2)(function, parameters) { result })
        assertEquals(1, value1)
        assertEquals(2, value2)
    }

    @Test
    fun invoke() = runTest {
        invoke(CalculatorImpl, EchoImpl)
    }
}

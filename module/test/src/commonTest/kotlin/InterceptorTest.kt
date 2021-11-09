package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.ProxyFactory
import kotlinx.coroutines.*
import kotlin.test.*

class InterceptorTest {
    @Test
    fun compositeInterceptor() = yassRunBlocking {
        val value = "string"
        var value1: Int? = null
        var value2: Int? = null
        val interceptor1: Interceptor = { _, _, invocation ->
            assertNull(value1)
            assertNull(value2)
            value1 = 1
            invocation()
        }
        val interceptor2: Interceptor = { _, _, invocation ->
            assertNotNull(value1)
            assertNull(value2)
            value2 = 1
            invocation()
        }
        assertSame(value, (interceptor1 + interceptor2)(Calculator::add, emptyList()) { value })
        assertNotNull(value1)
        assertNotNull(value2)
    }

    private interface NoSuchService

    @Test
    fun noSuchService() {
        assertPlatform<IllegalStateException>(
            "no proxy for 'class ch.softappeal.yass2.InterceptorTest\$noSuchService\$1\$1'",
            "no proxy for 'class null'",
            "no proxy for 'class <anonymous>'",
        ) {
            ProxyFactory(object : NoSuchService {}) { _, _, invocation: Invocation -> invocation() }
        }
    }

    @Test
    fun proxyFactory() = yassRunBlocking {
        ProxyFactory.test(CalculatorImpl, EchoImpl)
    }

    @Test
    fun performance() = yassRunBlocking {
        var counter = 0
        val proxy = ProxyFactory(CalculatorImpl) { _, _, invocation ->
            counter++
            invocation()
        }
        performance(100_000) { assertEquals(4, proxy.divide(12, 3)) }
        assertEquals(200_000, counter)
    }
}

val CalculatorImpl = object : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

val EchoImpl = object : Echo {
    override suspend fun echo(value: Any?) = value
    override suspend fun echoRequired(value: Any) = value
    override suspend fun noParametersNoResult() {}
    override suspend fun delay(milliSeconds: Int) = delay(milliSeconds.toLong())
}

suspend fun ProxyFactory.test(calculatorImpl: Calculator, echoImpl: Echo) {
    val printer: Interceptor = { function, parameters, invocation ->
        print("${function.name} $parameters -> ")
        try {
            val result = invocation()
            println(result)
            result
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }
    var counter = 0
    var functionName: String? = null
    var params: List<Any?>? = null
    val testInterceptor: Interceptor = { function, parameters, invocation ->
        counter++
        functionName = function.name
        params = parameters
        invocation()
    }
    val interceptor = testInterceptor + printer
    val calculator = this(calculatorImpl, interceptor)
    val echo = this(echoImpl, interceptor)
    assertEquals(5, calculator.add(2, 3))
    assertEquals("add", functionName)
    assertEquals(listOf(2, 3), params)
    assertEquals(1, counter)
    assertEquals(3, calculator.divide(12, 4))
    assertEquals(2, counter)
    assertSuspendFailsWith<DivideByZeroException> { calculator.divide(12, 0) }
    assertEquals(3, counter)
    echo.noParametersNoResult()
    assertEquals("hello", echo.echo("hello"))
    assertEquals(3, (echo.echo(ByteArray(3)) as ByteArray).size)
    withTimeout(200) { echo.delay(100) }
    println(assertSuspendFailsWith<TimeoutCancellationException> {
        withTimeout(100) { echo.delay(200) }
    })
}

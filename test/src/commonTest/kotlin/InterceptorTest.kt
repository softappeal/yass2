package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

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

val MixedImpl = object : Mixed {
    override fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
    override suspend fun suspendDivide(a: Int, b: Int) = divide(a, b)
    override fun noParametersNoResult() {}
}

suspend fun ProxyFactory.test(calculatorImpl: Calculator, echoImpl: Echo) {
    val printer: SuspendInterceptor = { function, parameters, invocation ->
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
    val testInterceptor: SuspendInterceptor = { function, parameters, invocation ->
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

private fun ProxyFactory.test() {
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
    val mixed = this(MixedImpl, testInterceptor + printer) { _, _, _ -> error("") }
    assertEquals(3, mixed.divide(12, 4))
    assertEquals("divide", functionName)
    assertEquals(listOf(12, 4), params)
    assertEquals(1, counter)
    println(assertFailsWith<DivideByZeroException> { mixed.divide(12, 0) })
    assertEquals(2, counter)
    mixed.noParametersNoResult()
    println(mixed.toString())
    println(mixed.hashCode())
    assertNotEquals(mixed, Any())
}

open class InterceptorTest {
    protected open val proxyFactory: ProxyFactory = GeneratedProxyFactory

    @Test
    fun compositeInterceptor() {
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

    @Test
    fun suspendCompositeInterceptor() = runTest {
        val value = "string"
        var value1: Int? = null
        var value2: Int? = null
        val interceptor1: SuspendInterceptor = { _, _, invocation ->
            assertNull(value1)
            assertNull(value2)
            value1 = 1
            invocation()
        }
        val interceptor2: SuspendInterceptor = { _, _, invocation ->
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
        ) {
            GeneratedProxyFactory(object : NoSuchService {}) { _, _, invocation: Invocation -> invocation() }
        }
    }

    @Test
    fun proxyFactoryTest() {
        proxyFactory.test()
    }

    @Test
    fun suspendProxyFactory() = runTest {
        proxyFactory.test(CalculatorImpl, EchoImpl)
    }

    @Test
    fun performance() {
        var counter = 0
        val proxy = proxyFactory(MixedImpl,
            { _, _, invocation: Invocation ->
                counter++
                invocation()
            },
            { _, _, _: SuspendInvocation -> }
        )
        performance(100_000) { assertEquals(4, proxy.divide(12, 3)) }
        assertEquals(200_000, counter)
    }

    @Test
    fun suspendPerformance() = runTest {
        var counter = 0
        val proxy = proxyFactory(CalculatorImpl) { _, _, invocation: SuspendInvocation ->
            counter++
            invocation()
        }
        performance(100_000) { assertEquals(4, proxy.divide(12, 3)) }
        assertEquals(200_000, counter)
    }

    @Test
    fun missingInterceptor() {
        assertEquals(
            "missing Interceptor",
            assertFailsWith<RuntimeException> { MissingInterceptor(Calculator::add, emptyList()) {} }.message
        )
    }

    @Test
    fun missingSuspendInterceptor() = runTest {
        assertEquals(
            "missing SuspendInterceptor",
            assertFailsWith<RuntimeException> { MissingSuspendInterceptor(Calculator::add, emptyList()) {} }.message
        )
    }

    @Test
    fun checkInterceptors() {
        assertEquals(
            "missing Interceptor",
            assertFailsWith<IllegalArgumentException> {
                proxyFactory(MixedImpl) { _, _, _: SuspendInvocation -> }
            }.message
        )
        assertEquals(
            "missing SuspendInterceptor",
            assertFailsWith<IllegalArgumentException> {
                proxyFactory(MixedImpl) { _, _, _: Invocation -> }
            }.message
        )
    }
}

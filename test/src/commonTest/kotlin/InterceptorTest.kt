package ch.softappeal.yass2

import ch.softappeal.yass2.contract.Calculator
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Echo
import ch.softappeal.yass2.contract.Mixed
import ch.softappeal.yass2.contract.Node
import ch.softappeal.yass2.contract.child.NoSuspend
import ch.softappeal.yass2.contract.reflect.proxy
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds
import ch.softappeal.yass2.contract.child.reflect.proxy as childProxy

val CalculatorImpl = object : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

val EchoImpl = object : Echo {
    override suspend fun echo(value: Any?) = value
    override suspend fun echoRequired(value: Any) = value
    override suspend fun noParametersNoResult() {}
    override suspend fun delay(milliSeconds: Int) = delay(milliSeconds.toLong())
    override suspend fun echoNode(node: Node?) = node
    override suspend fun echoNodeRequired(node: Node) = node
    override suspend fun echoGeneric(map: Map<String?, Node>): Map<Int, Node>? =
        mapOf(map.entries.first().key!!.toInt() to map.entries.first().value)

    override suspend fun echoMonster(a: List<*>, b: List<List<String?>?>, c: Map<out Int, String>, d: Pair<*, *>) = null
}

private val MixedImpl = object : Mixed {
    override fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
    override suspend fun suspendDivide(a: Int, b: Int) = divide(a, b)
    override fun noParametersNoResult() {}
}

private val NoSuspendImpl = object : NoSuspend {
    override fun x() {}
}

val Printer: SuspendInterceptor = { function, parameters, invoke ->
    print("${function.name} $parameters -> ")
    try {
        val result = invoke()
        println(result)
        result
    } catch (e: Exception) {
        println(e)
        throw e
    }
}

suspend fun test(calculatorImpl: Calculator, echoImpl: Echo) {
    var counter = 0
    var functionName: String? = null
    var params: List<Any?>? = null
    val testInterceptor: SuspendInterceptor = { function, parameters, invoke ->
        counter++
        functionName = function.name
        params = parameters
        invoke()
    }
    val interceptor = testInterceptor + Printer
    val calculator = calculatorImpl.proxy(interceptor)
    val echo = echoImpl.proxy(interceptor)
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
    withTimeout(200.milliseconds) { echo.delay(100) }
    println(assertSuspendFailsWith<TimeoutCancellationException> {
        withTimeout(100.milliseconds) { echo.delay(200) }
    })
    assertEquals(123, echo.echoNodeRequired(Node(123)).id)
    assertEquals(123, echo.echoNode(Node(123))!!.id)
    assertNull(echo.echoNode(null))
}

class InterceptorTest {
    @Test
    fun compositeInterceptor() {
        val value = "string"
        var value1: Int? = null
        var value2: Int? = null
        val interceptor1: Interceptor = { _, _, invoke ->
            assertNull(value1)
            assertNull(value2)
            value1 = 1
            invoke()
        }
        val interceptor2: Interceptor = { _, _, invoke ->
            assertNotNull(value1)
            assertNull(value2)
            value2 = 1
            invoke()
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
        val interceptor1: SuspendInterceptor = { _, _, invoke ->
            assertNull(value1)
            assertNull(value2)
            value1 = 1
            invoke()
        }
        val interceptor2: SuspendInterceptor = { _, _, invoke ->
            assertNotNull(value1)
            assertNull(value2)
            value2 = 1
            invoke()
        }
        assertSame(value, (interceptor1 + interceptor2)(Calculator::add, emptyList()) { value })
        assertNotNull(value1)
        assertNotNull(value2)
    }

    @Test
    fun test() {
        val printer: Interceptor = { function, parameters, invoke ->
            print("${function.name} $parameters -> ")
            try {
                val result = invoke()
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
        val testInterceptor: Interceptor = { function, parameters, invoke ->
            counter++
            functionName = function.name
            params = parameters
            invoke()
        }
        val mixed = MixedImpl.proxy(testInterceptor + printer) { _, _, _ -> error("") }
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
        assertEquals(3, counter)
        val noSuspend = NoSuspendImpl.childProxy(testInterceptor + printer)
        noSuspend.x()
        assertEquals(4, counter)
    }

    @Test
    fun suspendTest() = runTest {
        test(CalculatorImpl, EchoImpl)
    }

    @Test
    fun testGeneric() = runTest {
        val echo = EchoImpl.proxy { _, _, invoke ->
            invoke()
        }
        assertEquals("hello", echo.echo("hello"))
        val map = echo.echoGeneric(mapOf("13" to Node(99)))!!
        assertEquals(1, map.size)
        assertEquals(99, map[13]!!.id)
    }

    @Test
    fun performance() {
        var counter = 0
        val proxy = MixedImpl.proxy(
            { _, _, invoke ->
                counter++
                invoke()
            },
            { _, _, _ -> }
        )
        performance(100_000) { assertEquals(4, proxy.divide(12, 3)) }
        assertEquals(200_000, counter)
    }

    @Test
    fun suspendPerformance() = runTest {
        var counter = 0
        val proxy = CalculatorImpl.proxy { _, _, invoke ->
            counter++
            invoke()
        }
        performance(100_000) { assertEquals(4, proxy.divide(12, 3)) }
        assertEquals(200_000, counter)
    }
}

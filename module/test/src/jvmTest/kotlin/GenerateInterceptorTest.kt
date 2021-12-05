package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.ProxyFactory
import ch.softappeal.yass2.generate.*
import kotlinx.coroutines.*
import kotlin.reflect.full.*
import kotlin.test.*

@Suppress("unused")
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

class GenerateInterceptorTest {
    @Test
    fun overloadedFunction() {
        assertEquals(
            "'class ch.softappeal.yass2.Overloaded' has overloaded functions",
            assertFailsWith<IllegalArgumentException> { generateProxyFactory(listOf(Overloaded::class)) }.message
        )
    }

    @Test
    fun duplicatedService() {
        assertEquals(
            "duplicated service",
            assertFailsWith<IllegalArgumentException> { generateProxyFactory(listOf(Calculator::class, Calculator::class)) }.message
        )
    }

    @Test
    fun annotation() = runBlocking {
        var hasAnnotation = false
        val echo: Echo = ProxyFactory(EchoImpl) { function, parameters, invocation: Invocation ->
            hasAnnotation = function.findAnnotation<TestAnnotation>() != null
            println("${function.name} $hasAnnotation $parameters")
            invocation()
        }
        echo.echo(null)
        assertTrue(hasAnnotation)
        echo.noParametersNoResult()
        assertFalse(hasAnnotation)
    }
}

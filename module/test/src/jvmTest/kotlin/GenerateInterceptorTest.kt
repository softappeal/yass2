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
    fun duplicatedServices() {
        assertEquals(
            "duplicated services",
            assertFailsWith<IllegalArgumentException> { generateProxyFactory(listOf(Calculator::class, Calculator::class)) }.message
        )
    }

    @Test
    fun annotation() = runBlocking {
        var annotated: Boolean
        val echo: Echo = ProxyFactory(EchoImpl) { function, parameters, invocation: Invocation ->
            annotated = function.findAnnotation<TestAnnotation>() != null
            println("${function.name} $annotated $parameters")
            invocation()
        }
        annotated = false
        echo.echo(null)
        assertTrue(annotated)
        echo.noParametersNoResult()
        assertFalse(annotated)
    }
}

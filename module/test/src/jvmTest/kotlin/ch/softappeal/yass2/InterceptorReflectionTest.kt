package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.reflect.*
import kotlinx.coroutines.*
import kotlin.reflect.full.*
import kotlin.test.*

@Suppress("unused")
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

class InterceptorReflectionTest : InterceptorGeneratedTest() {
    override fun getProxyFactory() = ReflectionProxyFactory

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
        val echo: Echo = ReflectionProxyFactory(EchoImpl) { function, parameters, invocation: SuspendInvocation ->
            annotated = function.findAnnotation<TestAnnotation>() != null
            println("${function.name} $annotated ${parameters.asList()}")
            invocation()
        }
        annotated = false
        echo.echo(null)
        assertTrue(annotated)
        echo.noParametersNoResult()
        assertFalse(annotated)
    }
}

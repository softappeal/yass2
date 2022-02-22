package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
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

    interface NoSuspend {
        @Suppress("unused") fun x()
    }

    @Test
    fun noSuspend() {
        assertEquals(
            "'fun ch.softappeal.yass2.GenerateInterceptorTest.NoSuspend.x(): kotlin.Unit' is not a suspend function",
            assertFailsWith<IllegalArgumentException> { generateProxyFactory(listOf(NoSuspend::class)) }.message
        )
    }

    @Test
    fun annotation() = runBlocking {
        var hasAnnotation = false
        val echo: Echo = GeneratedProxyFactory(EchoImpl) { function, parameters, invocation: SuspendInvocation ->
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

class ReflectionInterceptorTest : InterceptorTest() {
    override val proxyFactory = ReflectionProxyFactory

    class NoSuspend {
        @Suppress("unused", "EmptyMethod") fun x() {}
    }

    @Test
    fun noSuspend() {
        assertEquals(
            "'fun ch.softappeal.yass2.ReflectionInterceptorTest.NoSuspend.x(): kotlin.Unit' is not a suspend function",
            assertFailsWith<IllegalArgumentException> {
                val interceptor: SuspendInterceptor = { _, _, _ -> }
                ReflectionProxyFactory(NoSuspend(), interceptor)
            }.message
        )
    }
}

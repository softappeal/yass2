package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
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
    fun overloadedFunction() = assertFailsMessage<IllegalArgumentException>("'class ch.softappeal.yass2.Overloaded' has overloaded functions") {
        generateProxyFactory("dummy", listOf(Overloaded::class))
    }

    @Test
    fun duplicatedService() = assertFailsMessage<IllegalArgumentException>("duplicated service") {
        generateProxyFactory("dummy", listOf(Calculator::class, Calculator::class))
    }

    @Test
    fun annotation() = runBlocking {
        var hasAnnotation = false
        val echo: Echo = ContractProxyFactory(EchoImpl) { function, parameters, invocation: SuspendInvocation ->
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

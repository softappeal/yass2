package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import kotlinx.coroutines.*
import kotlin.reflect.full.*
import kotlin.test.*

@Suppress("unused")
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

class GenerateProxyTest {
    /* TODO
    @Test
    fun overloadedFunction() = assertFailsMessage<IllegalArgumentException>("'class ch.softappeal.yass2.Overloaded' has overloaded functions") {
        StringBuilder().generateProxy(listOf(Overloaded::class))
    }

    @Test
    fun duplicatedService() = assertFailsMessage<IllegalArgumentException>("duplicated service") {
        StringBuilder().generateProxy(listOf(Calculator::class, Calculator::class))
    }
     */

    @Test
    fun annotation() = runBlocking {
        var hasAnnotation = false
        val echo: Echo = EchoImpl.proxy { function, parameters, invoke ->
            hasAnnotation = function.hasAnnotation<TestAnnotation>()
            println("${function.name} $hasAnnotation $parameters")
            invoke()
        }
        echo.echo(null)
        assertTrue(hasAnnotation)
        echo.noParametersNoResult()
        assertFalse(hasAnnotation)
    }
}

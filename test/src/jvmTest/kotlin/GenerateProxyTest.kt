package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import kotlinx.coroutines.*
import kotlin.reflect.full.*
import kotlin.test.*

// TODO: comment out the following line for testing overloads
// @GenerateProxy
@Suppress("unused") private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

class GenerateProxyTest {
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

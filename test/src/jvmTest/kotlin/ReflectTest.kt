package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import kotlinx.coroutines.*
import kotlin.reflect.full.*
import kotlin.test.*

class ReflectTest {
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

    @Test
    fun duplicatedProperty() {
        open class X(private val y: Int) {
            @Suppress("unused")
            fun myPrivateY() = y
        }

        class Y(val y: Int) : X(y) {
            @Suppress("unused")
            fun myY() = y
        }
        assertEquals(1, X::class.memberProperties.size)
        assertEquals(1, Y::class.memberProperties.size)
    }
}

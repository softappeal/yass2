package ch.softappeal.yass2

import ch.softappeal.yass2.contract.Echo
import ch.softappeal.yass2.contract.TestAnnotation
import ch.softappeal.yass2.contract.proxy
import kotlinx.coroutines.runBlocking
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

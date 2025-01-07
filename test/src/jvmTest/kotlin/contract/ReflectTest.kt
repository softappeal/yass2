package ch.softappeal.yass2.contract

import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals

class ReflectTest {
    @Test
    fun duplicatedProperty() {
        open class X(private val y: Int) {
            @Suppress("unused") fun myPrivateY() = y
        }

        class Y(val y: Int) : X(y) {
            @Suppress("unused") fun myY() = y
        }
        assertEquals(1, X::class.memberProperties.size)
        assertEquals(1, Y::class.memberProperties.size)
    }
}

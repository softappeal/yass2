package ch.softappeal.yass2.core.serialize.binary

import kotlin.test.Test
import kotlin.test.assertEquals

class BinaryVarIntTest {
    @Test
    fun varInt() {
        fun check(value: Int, vararg bytes: Int) = check(value, { writeVarInt(it) }, *bytes) { readVarInt() }
        check(0, 0)
        check(0x7F, 0x7F)
        check(0x80, 0x80, 0x01)
        check(0x3F_FF, 0xFF, 0x7F)
        check(0x40_00, 0x80, 0x80, 0x01)
        check(-1, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F)
    }

    @Test
    fun zigzagInt() {
        fun check(v: Int, z: Int) {
            assertEquals(z, v.toZigZag())
            assertEquals(v, z.fromZigZag())
        }
        check(0, 0)
        check(-1, 1)
        check(1, 2)
        check(-2, 3)
        check(2, 4)
        check(Int.MIN_VALUE, -1)
        check(Int.MAX_VALUE, -2)
        check(Int.MIN_VALUE + 1, -3)
        check(Int.MAX_VALUE - 1, -4)
    }
}

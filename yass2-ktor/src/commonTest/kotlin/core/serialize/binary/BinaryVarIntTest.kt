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
    fun varLong() {
        fun check(value: Long, vararg bytes: Int) = check(value, { writeVarLong(it) }, *bytes) { readVarLong() }
        check(0L, 0)
        check(0x7FL, 0x7F)
        check(0x80L, 0x80, 0x01)
        check(0x3F_FFL, 0xFF, 0x7F)
        check(0x40_00L, 0x80, 0x80, 0x01)
        check(-1, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)
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

    @Test
    fun zigzagLong() {
        fun check(v: Long, z: Long) {
            assertEquals(z, v.toZigZag())
            assertEquals(v, z.fromZigZag())
        }
        check(0L, 0L)
        check(-1L, 1L)
        check(1L, 2L)
        check(-2L, 3L)
        check(2L, 4L)
        check(Long.MIN_VALUE, -1L)
        check(Long.MAX_VALUE, -2L)
        check(Long.MIN_VALUE + 1L, -3L)
        check(Long.MAX_VALUE - 1L, -4L)
    }
}

package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.transport.BytesReader
import ch.softappeal.yass2.transport.BytesWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class VarIntTest {
    @Test
    fun varInt() {
        val writer = BytesWriter(1000)
        with(writer) {
            writeVarInt(0)
            checkTail(0)
            writeVarInt(0x7F)
            checkTail(0x7F)
            writeVarInt(0x80)
            checkTail(0x80, 0x01)
            writeVarInt(0x3F_FF)
            checkTail(0xFF, 0x7F)
            writeVarInt(0x40_00)
            checkTail(0x80, 0x80, 0x01)
            assertEquals(9, current)
            writeVarInt(-1)
            assertEquals(14, current)
        }
        with(BytesReader(writer.buffer)) {
            assertEquals(0, readVarInt())
            assertEquals(0x7F, readVarInt())
            assertEquals(0x80, readVarInt())
            assertEquals(0x3F_FF, readVarInt())
            assertEquals(0x40_00, readVarInt())
            assertEquals(-1, readVarInt())
            assertEquals(14, current)
        }
    }

    @Test
    fun varLong() {
        val writer = BytesWriter(1000)
        with(writer) {
            writeVarLong(0L)
            checkTail(0)
            writeVarLong(0x7FL)
            checkTail(0x7F)
            writeVarLong(0x80L)
            checkTail(0x80, 0x01)
            writeVarLong(0x3F_FFL)
            checkTail(0xFF, 0x7F)
            writeVarLong(0x40_00L)
            checkTail(0x80, 0x80, 0x01)
            assertEquals(9, current)
            writeVarLong(-1)
            assertEquals(19, current)
        }
        with(BytesReader(writer.buffer)) {
            assertEquals(0L, readVarLong())
            assertEquals(0x7FL, readVarLong())
            assertEquals(0x80L, readVarLong())
            assertEquals(0x3F_FFL, readVarLong())
            assertEquals(0x40_00L, readVarLong())
            assertEquals(-1, readVarLong())
            assertEquals(19, current)
        }
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

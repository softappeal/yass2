package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.ByteArrayReader
import ch.softappeal.yass2.serialize.ByteArrayWriter
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.assertFailsWithMessage
import ch.softappeal.yass2.serialize.checkDrained
import kotlin.test.Test
import kotlin.test.assertEquals

private fun <T> check(value: T, write: Writer.(value: T) -> Unit, vararg bytes: Int, read: Reader.() -> T) {
    val writer = ByteArrayWriter()
    writer.write(value)
    val byteArray = writer.toByteArray()
    assertEquals(bytes.map { it.toByte() }, byteArray.toList())
    val reader = ByteArrayReader(byteArray)
    assertEquals(value, reader.read().apply { reader.checkDrained() })
}

class BinaryPrimitivesTest {
    @Test
    fun boolean() {
        check(false, { writeBoolean(it) }, 0) { readBoolean() }
        check(true, { writeBoolean(it) }, 1) { readBoolean() }
        assertFailsWithMessage<IllegalStateException>("unexpected binary boolean 2") {
            ByteArrayReader(byteArrayOf(2)).readBoolean()
        }
    }

    @Test
    fun varInt() {
        fun check(value: Int, vararg bytes: Int) = check(value, { writeVarInt(it) }, *bytes) { readVarInt() }
        check(0, 0)
        check(1, 1)
        check(2, 2)
        check(0x7F, 0x7F)
        check(0x80, 0x80, 0x01)
        check(0x3F_FF, 0xFF, 0x7F)
        check(0x40_00, 0x80, 0x80, 0x01)
        check(-1, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F)
        check(-2, 0xFE, 0xFF, 0xFF, 0xFF, 0x0F)
    }

    @Test
    fun varLong() {
        fun check(value: Long, vararg bytes: Int) = check(value, { writeVarLong(it) }, *bytes) { readVarLong() }
        check(0, 0)
        check(1, 1)
        check(2, 2)
        check(0x7F, 0x7F)
        check(0x80, 0x80, 0x01)
        check(0x3F_FF, 0xFF, 0x7F)
        check(0x40_00, 0x80, 0x80, 0x01)
        check(-1, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)
        check(-2, 0xFE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01)
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

    @Test
    fun int() {
        fun check(value: Int, vararg bytes: Int) = check(value, { writeInt(it) }, *bytes) { readInt() }
        check(Int.MIN_VALUE, 0x80, 0x00, 0x00, 0x00)
        check(Int.MAX_VALUE, 0x7F, 0xFF, 0xFF, 0xFF)
        check(0x12_34_56_78, 0x12, 0x34, 0x56, 0x78)
    }

    @Test
    fun long() {
        fun check(value: Long, vararg bytes: Int) = check(value, { writeLong(it) }, *bytes) { readLong() }
        check(Long.MIN_VALUE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        check(Long.MAX_VALUE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)
        check(0x12_34_56_78_9A_BC_DE_F0, 0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0)
    }
}

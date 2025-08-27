package ch.softappeal.yass2.core.serialize.binary

import ch.softappeal.yass2.assertFailsWithMessage
import ch.softappeal.yass2.core.serialize.ByteArrayReader
import ch.softappeal.yass2.core.serialize.ByteArrayWriter
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer
import ch.softappeal.yass2.core.serialize.checkDrained
import kotlin.test.Test
import kotlin.test.assertEquals

fun <T> check(value: T, write: Writer.(value: T) -> Unit, vararg bytes: Int, read: Reader.() -> T) {
    val writer = ByteArrayWriter()
    writer.write(value)
    val byteArray = writer.toyByteArray()
    assertEquals(bytes.map { it.toByte() }, byteArray.toList())
    val reader = ByteArrayReader(byteArray)
    assertEquals(value, reader.read().apply { reader.checkDrained() })
}

class BinaryIntTest {
    @Test
    fun boolean() {
        check(false, { writeBinaryBoolean(it) }, 0) { readBinaryBoolean() }
        check(true, { writeBinaryBoolean(it) }, 1) { readBinaryBoolean() }
        assertFailsWithMessage<IllegalStateException>("unexpected binary boolean 2") {
            ByteArrayReader(byteArrayOf(2)).readBinaryBoolean()
        }
    }

    @Test
    fun int() {
        fun check(value: Int, vararg bytes: Int) = check(value, { writeBinaryInt(it) }, *bytes) { readBinaryInt() }
        check(Int.MIN_VALUE, 0x80, 0x00, 0x00, 0x00)
        check(Int.MAX_VALUE, 0x7F, 0xFF, 0xFF, 0xFF)
        check(0x12_34_56_78, 0x12, 0x34, 0x56, 0x78)
    }

    @Test
    fun long() {
        fun check(value: Long, vararg bytes: Int) = check(value, { writeBinaryLong(it) }, *bytes) { readBinaryLong() }
        check(Long.MIN_VALUE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        check(Long.MAX_VALUE, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)
        check(0x12_34_56_78_9A_BC_DE_F0, 0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0)
    }
}

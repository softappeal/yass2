package ch.softappeal.yass2.core.serialize

import ch.softappeal.yass2.core.assertFailsMessage
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

fun check(write: Writer.() -> Unit, vararg bytes: Int, read: Reader.() -> Unit) {
    val writer = ByteArrayWriter()
    writer.write()
    val byteArray = writer.toyByteArray()
    assertEquals(bytes.map { it.toByte() }, byteArray.toList())
    with(ByteArrayReader(byteArray)) {
        assertFalse(isDrained)
        read()
        checkDrained()
    }
}

class ByteArraysTest {
    @Test
    fun byte() {
        check({ writeByte(1) }, 1) { assertEquals(1, readByte()) }
        check({ writeByte(-1) }, -1) { assertEquals(-1, readByte()) }
        assertFailsMessage<IllegalArgumentException>("'readByte()' called when buffer is empty") {
            ByteArrayReader(byteArrayOf()).readByte()
        }
    }

    @Test
    fun byteArray() {
        check({ writeByteArray(byteArrayOf(1, -1)) }, 1, -1) { assertContentEquals(byteArrayOf(1, -1), readByteArray(2)) }
        assertFailsMessage<IllegalArgumentException>("'readByteArray(3)' called when buffer is empty") {
            ByteArrayReader(byteArrayOf()).readByteArray(3)
        }
    }
}

package ch.softappeal.yass2.core.serialize

import ch.softappeal.yass2.core.assertFailsMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

fun BytesWriter.checkTail(vararg bytes: Int) {
    assertEquals(bytes.map { it.toByte() }, buffer.copyOfRange(current - bytes.size, current).toList())
}

class BytesTest {
    @Test
    fun test() {
        val writer = BytesWriter(4)
        with(writer) {
            val buffer = this.buffer
            writeByte(-128)
            assertEquals(1, current)
            assertEquals(-128, buffer[0])
            writeByte(127)
            assertEquals(2, current)
            assertEquals(127, buffer[1])
            writeBytes(ByteArray(0))
            assertEquals(2, current)
            writeBytes(byteArrayOf(1, 2))
            assertEquals(4, current)
            assertEquals(1, buffer[2])
            assertEquals(2, buffer[3])
            checkTail(1, 2)
            assertFails { checkTail(11, 2) }
            assertFails { checkTail(1, 22) }
            writeBytes(ByteArray(0))
            assertEquals(4, current)
            assertSame(buffer, this.buffer)
        }
        with(BytesReader(writer.buffer)) {
            assertFalse(isDrained)
            assertEquals(-128, readByte())
            assertEquals(1, current)
            assertEquals(127, readByte())
            assertEquals(2, current)
            assertFails { readBytes(-1) }
            assertEquals(2, current)
            assertEquals(0, readBytes(0).size)
            assertEquals(2, current)
            assertFailsMessage<IllegalArgumentException>("'readBytes(3)' called when buffer is empty") {
                readBytes(3)
            }
            assertEquals(2, current)
            assertEquals(byteArrayOf(1, 2).toList(), readBytes(2).toList())
            assertEquals(4, current)
            assertTrue(isDrained)
            assertFailsMessage<IllegalArgumentException>("'readByte()' called when buffer is empty") {
                readByte()
            }
            assertEquals(4, current)
        }
        with(BytesWriter(0)) {
            writeByte(0)
            assertEquals(1000, buffer.size)
            assertEquals(1, current)
        }
        with(BytesWriter(501)) {
            current = 501
            writeByte(0)
            assertEquals(1002, buffer.size)
            assertEquals(502, current)
        }
        with(BytesWriter(0)) {
            writeBytes(ByteArray(1))
            assertEquals(1001, buffer.size)
            assertEquals(1, current)
        }
        with(BytesWriter(1002)) {
            current = 1002
            writeBytes(ByteArray(1))
            assertEquals(2004, buffer.size)
            assertEquals(1003, current)
        }
    }
}

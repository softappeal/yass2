package ch.softappeal.yass2.transport

import ch.softappeal.yass2.serialize.binary.checkTail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BytesTest {
    @Test
    fun test() {
        val writer = BytesWriter(4)
        with(writer) {
            val buffer = this.buffer
            assertEquals(0, current)
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
            assertFailsWith<IndexOutOfBoundsException> { writeByte(0) }
            assertEquals(4, current)
            assertFailsWith<IndexOutOfBoundsException> { writeBytes(ByteArray(1000)) }
            assertEquals(4, current)
        }
        with(BytesReader(writer.buffer)) {
            assertFalse(isDrained)
            assertEquals(0, current)
            assertEquals(-128, readByte())
            assertEquals(1, current)
            assertEquals(127, readByte())
            assertEquals(2, current)
            assertFails { readBytes(-1) }
            assertEquals(2, current)
            assertEquals(0, readBytes(0).size)
            assertEquals(2, current)
            assertFailsWith<IndexOutOfBoundsException> { readBytes(3) }
            assertEquals(2, current)
            assertEquals(byteArrayOf(1, 2).toList(), readBytes(2).toList())
            assertEquals(4, current)
            assertTrue(isDrained)
            assertFailsWith<IndexOutOfBoundsException> { readByte() }
            assertEquals(4, current)
        }
    }
}

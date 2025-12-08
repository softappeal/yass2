@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.core.serialize

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.assertFailsWithMessage
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ByteArraysTest {
    @Test
    fun test() {
        val byteArray: ByteArray
        with(ByteArrayWriter()) {
            writeByte(13)
            writeByteArray(byteArrayOf(1, 2, 3))
            writeByte(31)
            byteArray = toyByteArray()
        }
        assertContentEquals(byteArrayOf(13, 1, 2, 3, 31), byteArray)
        with(ByteArrayReader(byteArray)) {
            assertFalse(isDrained)
            assertFailsWithMessage<IllegalStateException>("buffer not drained") { checkDrained() }
            assertEquals(13, readByte())
            assertContentEquals(byteArrayOf(1, 2, 3), readByteArray(3))
            assertEquals(31, readByte())
            assertTrue(isDrained)
            checkDrained()
            assertFailsWithMessage<IllegalArgumentException>("'readByte()' called when buffer is empty") { readByte() }
            assertFailsWithMessage<IllegalArgumentException>("'readByteArray(1)' called when buffer is empty") { readByteArray(1) }
        }
    }

    @Test
    fun enlarge() {
        with(ByteArrayWriter(1)) {
            assertEquals(1, byteArray.size)
            writeByte(13)
            assertEquals(1, byteArray.size)
            writeByte(31)
            assertEquals(1000, byteArray.size)
            assertContentEquals(byteArrayOf(13, 31), toyByteArray())
            writeByteArray(ByteArray(998))
            assertEquals(1000, byteArray.size)
            writeByte(99)
            assertEquals(2000, byteArray.size)
            assertEquals(13, byteArray[0])
            assertEquals(31, byteArray[1])
            assertEquals(99, byteArray[1000])
        }
        with(ByteArrayWriter(2)) {
            writeByteArray(byteArrayOf(1))
            assertEquals(2, byteArray.size)
            writeByteArray(byteArrayOf(2, 3))
            assertEquals(1003, byteArray.size)
            writeByteArray(ByteArray(1000))
            writeByteArray(byteArrayOf(4))
            assertEquals(2006, byteArray.size)
            assertEquals(1, byteArray[0])
            assertEquals(2, byteArray[1])
            assertEquals(3, byteArray[2])
            assertEquals(4, byteArray[1003])
        }
    }
}

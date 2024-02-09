package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.transport.BytesReader
import ch.softappeal.yass2.transport.BytesWriter
import ch.softappeal.yass2.transport.checkTail
import ch.softappeal.yass2.transport.internalCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntTest {
    @Test
    fun test() {
        val writer = BytesWriter(1000)
        with(writer) {
            writeBoolean(false)
            writeBoolean(true)
            writeInt(Int.MIN_VALUE)
            checkTail(0x80, 0x00, 0x00, 0x00)
            writeInt(Int.MAX_VALUE)
            checkTail(0x7F, 0xFF, 0xFF, 0xFF)
            writeInt(0x12_34_56_78)
            checkTail(0x12, 0x34, 0x56, 0x78)
            writeLong(Long.MIN_VALUE)
            checkTail(0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            writeLong(Long.MAX_VALUE)
            checkTail(0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)
            writeLong(0x12_34_56_78_9A_BC_DE_F0)
            checkTail(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0)
            assertEquals(38, current)
        }
        with(BytesReader(writer.buffer)) {
            assertFalse(readBoolean())
            assertTrue(readBoolean())
            assertEquals(Int.MIN_VALUE, readInt())
            assertEquals(Int.MAX_VALUE, readInt())
            assertEquals(0x12_34_56_78, readInt())
            assertEquals(Long.MIN_VALUE, readLong())
            assertEquals(Long.MAX_VALUE, readLong())
            assertEquals(0x12_34_56_78_9A_BC_DE_F0, readLong())
            assertEquals(38, internalCurrent(this))
        }
    }
}

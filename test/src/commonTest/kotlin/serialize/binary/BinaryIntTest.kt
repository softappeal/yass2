package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.BytesReader
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.checkTail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BinaryIntTest {
    @Test
    fun test() {
        val writer = BytesWriter(1000)
        with(writer) {
            writeBinaryBoolean(false)
            writeBinaryBoolean(true)
            writeBinaryInt(Int.MIN_VALUE)
            checkTail(0x80, 0x00, 0x00, 0x00)
            writeBinaryInt(Int.MAX_VALUE)
            checkTail(0x7F, 0xFF, 0xFF, 0xFF)
            writeBinaryInt(0x12_34_56_78)
            checkTail(0x12, 0x34, 0x56, 0x78)
            writeBinaryLong(Long.MIN_VALUE)
            checkTail(0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            writeBinaryLong(Long.MAX_VALUE)
            checkTail(0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)
            writeBinaryLong(0x12_34_56_78_9A_BC_DE_F0)
            checkTail(0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0)
            assertEquals(38, current)
        }
        with(BytesReader(writer.buffer)) {
            assertFalse(readBinaryBoolean())
            assertTrue(readBinaryBoolean())
            assertEquals(Int.MIN_VALUE, readBinaryInt())
            assertEquals(Int.MAX_VALUE, readBinaryInt())
            assertEquals(0x12_34_56_78, readBinaryInt())
            assertEquals(Long.MIN_VALUE, readBinaryLong())
            assertEquals(Long.MAX_VALUE, readBinaryLong())
            assertEquals(0x12_34_56_78_9A_BC_DE_F0, readBinaryLong())
            assertEquals(38, current)
        }
        assertFails { BytesReader(2).readBinaryBoolean() }
    }
}

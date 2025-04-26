package ch.softappeal.yass2.core.serialize.binary

import ch.softappeal.yass2.core.assertFailsMessage
import ch.softappeal.yass2.core.serialize.ByteArrayReader
import ch.softappeal.yass2.core.serialize.check
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BinaryIntTest {
    @Test
    fun boolean() {
        check({ writeBinaryBoolean(false) }, 0) { assertFalse(readBinaryBoolean()) }
        check({ writeBinaryBoolean(true) }, 1) { assertTrue(readBinaryBoolean()) }
        assertFailsMessage<IllegalStateException>("unexpected binary boolean 2") {
            ByteArrayReader(byteArrayOf(2)).readBinaryBoolean()
        }
    }

    @Test
    fun int() {
        check({ writeBinaryInt(Int.MIN_VALUE) }, 0x80, 0x00, 0x00, 0x00) { assertEquals(Int.MIN_VALUE, readBinaryInt()) }
        check({ writeBinaryInt(Int.MAX_VALUE) }, 0x7F, 0xFF, 0xFF, 0xFF) { assertEquals(Int.MAX_VALUE, readBinaryInt()) }
        check({ writeBinaryInt(0x12_34_56_78) }, 0x12, 0x34, 0x56, 0x78) { assertEquals(0x12_34_56_78, readBinaryInt()) }
    }

    @Test
    fun long() {
        check({ writeBinaryLong(Long.MIN_VALUE) }, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00) {
            assertEquals(Long.MIN_VALUE, readBinaryLong())
        }
        check({ writeBinaryLong(Long.MAX_VALUE) }, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF) {
            assertEquals(Long.MAX_VALUE, readBinaryLong())
        }
        check({ writeBinaryLong(0x12_34_56_78_9A_BC_DE_F0) }, 0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0) {
            assertEquals(0x12_34_56_78_9A_BC_DE_F0, readBinaryLong())
        }
    }
}

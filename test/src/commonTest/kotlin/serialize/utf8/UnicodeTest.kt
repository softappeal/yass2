package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.serialize.BytesReader
import kotlin.test.Test
import kotlin.test.assertEquals

private fun readCodePoint(vararg bytes: Int): Int {
    val reader = BytesReader(*bytes)
    return reader.readCodePoint().apply {
        reader.checkDrained()
    }
}

class UnicodeTest {
    @Test
    fun invalidFirstByte() {
        val firstBytes = mutableListOf<Int>()
        firstBytes.addAll((0b1000_0000..0b1011_1111).toList())
        firstBytes.addAll((0b1111_1000..0b1111_1111).toList())
        firstBytes.forEach { firstByte ->
            assertFailsMessage<IllegalStateException>("invalid first byte $firstByte") {
                readCodePoint(firstByte)
            }
        }
    }

    @Test
    fun invalidFollowByte() {
        (0b1100_0000..0b1111_1111).forEach { followByte ->
            assertFailsMessage<IllegalStateException>("invalid follow byte $followByte") {
                readCodePoint(0b1100_0000, followByte)
            }
        }
    }

    @Test
    fun overlongEncoding() {
        fun read(value: Int, vararg bytes: Int) {
            assertEquals(value, readCodePoint(*bytes))
        }

        assertFailsMessage<IllegalStateException>("overlong 2 byte encoding of ${0x7F}") {
            readCodePoint(0b1100_0001, 0b1011_1111)
        }
        read(0x80, 0b1100_0010, 0b1000_0000)

        assertFailsMessage<IllegalStateException>("overlong 3 byte encoding of ${0x7FF}") {
            readCodePoint(0b1110_0000, 0b1001_1111, 0b1011_1111)
        }
        read(0x800, 0b1110_0000, 0b1010_0000, 0b1000_0000)
        assertFailsMessage<IllegalStateException>("illegal surrogate ${0xD800}") {
            readCodePoint(0b1110_1101, 0b1010_0000, 0b1000_0000)
        }
        assertFailsMessage<IllegalStateException>("illegal surrogate ${0xDFFF}") {
            readCodePoint(0b1110_1101, 0b1011_1111, 0b1011_1111)
        }

        assertFailsMessage<IllegalStateException>("overlong 4 byte encoding of ${0xFFFF}") {
            readCodePoint(0b1111_0000, 0b1000_1111, 0b1011_1111, 0b1011_1111)
        }
        read(0x10000, 0b1111_0000, 0b1001_0000, 0b1000_0000, 0b1000_0000)
    }
}

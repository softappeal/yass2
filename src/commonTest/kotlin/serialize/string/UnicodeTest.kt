package ch.softappeal.yass2.serialize.string

import ch.softappeal.yass2.assertFailsWithMessage
import ch.softappeal.yass2.serialize.ByteArrayReader
import ch.softappeal.yass2.serialize.checkDrained
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun readCodePoint(vararg bytes: Int): Int {
    val reader = ByteArrayReader(bytes.map { it.toByte() }.toByteArray())
    return reader.readCodePoint().apply { reader.checkDrained() }
}

class UnicodeTest {
    @Test
    fun addCodePoint() {
        fun string(codePoint: Int) = buildString { addCodePoint(codePoint) }
        string(Char.MAX_VALUE.code).apply {
            assertEquals(1, this.length)
            assertEquals(Char.MAX_VALUE, this[0])
        }
        string(0b101_1_100010_1111000111).apply {
            assertEquals(2, this.length)
            val highSurrogate = this[0]
            val lowSurrogate = this[1]
            assertTrue(highSurrogate.isHighSurrogate())
            assertTrue(lowSurrogate.isLowSurrogate())
            assertEquals(0b110110_101_0_100010, highSurrogate.code)
            assertEquals(0b110111_1111000111, lowSurrogate.code)
        }
    }

    @Test
    fun invalidFirstByte() {
        val firstBytes = mutableListOf<Int>()
        firstBytes.addAll((0b1000_0000..0b1011_1111).toList())
        firstBytes.addAll((0b1111_1000..0b1111_1111).toList())
        firstBytes.forEach { firstByte ->
            assertFailsWithMessage<IllegalStateException>("invalid first byte $firstByte") {
                readCodePoint(firstByte)
            }
        }
    }

    @Test
    fun invalidFollowByte() {
        (0b1100_0000..0b1111_1111).forEach { followByte ->
            assertFailsWithMessage<IllegalStateException>("invalid follow byte $followByte") {
                readCodePoint(0b1100_0000, followByte)
            }
        }
    }

    @Test
    fun overlongEncoding() {
        fun read(value: Int, vararg bytes: Int) {
            assertEquals(value, readCodePoint(*bytes))
        }

        assertFailsWithMessage<IllegalStateException>("overlong 2 byte encoding of ${0x7F}") {
            readCodePoint(0b1100_0001, 0b1011_1111)
        }
        read(0x80, 0b1100_0010, 0b1000_0000)

        assertFailsWithMessage<IllegalStateException>("overlong 3 byte encoding of ${0x7FF}") {
            readCodePoint(0b1110_0000, 0b1001_1111, 0b1011_1111)
        }
        read(0x800, 0b1110_0000, 0b1010_0000, 0b1000_0000)
        assertFailsWithMessage<IllegalStateException>("illegal surrogate ${0xD800}") {
            readCodePoint(0b1110_1101, 0b1010_0000, 0b1000_0000)
        }
        assertFailsWithMessage<IllegalStateException>("illegal surrogate ${0xDFFF}") {
            readCodePoint(0b1110_1101, 0b1011_1111, 0b1011_1111)
        }

        assertFailsWithMessage<IllegalStateException>("overlong 4 byte encoding of ${0xFFFF}") {
            readCodePoint(0b1111_0000, 0b1000_1111, 0b1011_1111, 0b1011_1111)
        }
        read(0x10000, 0b1111_0000, 0b1001_0000, 0b1000_0000, 0b1000_0000)
    }
}

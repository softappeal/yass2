package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.BytesReader
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.checkTail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private enum class Color { Red, Green }

private val ColorEncoder = EnumBinaryEncoder(Color::class, enumValues())

private class OptionalString(val s: String?)

private class OptionalStringEncoder : BinaryEncoder<OptionalString>(OptionalString::class,
    { value -> writeBinaryOptional(value.s) { writeBinaryString(it) } },
    { OptionalString(readBinaryOptional { readBinaryString() }) }
)

class BinaryEncodersTest {
    @Test
    fun test() {
        fun <T : Any> BinaryEncoder<T>.check(value: T, vararg bytes: Int) {
            val writer = BytesWriter(1000)
            with(writer) {
                write(this, value)
                checkTail(*bytes)
                assertEquals(bytes.size, current)
            }
            with(BytesReader(writer.buffer)) {
                if (value is ByteArray) {
                    assertEquals(value.toList(), (read(this) as ByteArray).toList())
                } else {
                    assertEquals(value, read(this))
                }
                assertEquals(bytes.size, current)
            }
        }
        with(BooleanBinaryEncoder()) {
            check(false, 0)
            check(true, 1)
        }
        with(ByteBinaryEncoder()) {
            check(0, 0)
            check(-1, -1)
            check(1, 1)
            check(Byte.MAX_VALUE, 127)
            check(Byte.MIN_VALUE, -128)
        }
        with(IntBinaryEncoder()) {
            check(0, 0)
            check(-1, 1)
            check(1, 2)
            check(Int.MAX_VALUE, -2, -1, -1, -1, 15)
            check(Int.MIN_VALUE, -1, -1, -1, -1, 15)
        }
        with(LongBinaryEncoder()) {
            check(0, 0)
            check(-1, 1)
            check(1, 2)
            check(Long.MAX_VALUE, -2, -1, -1, -1, -1, -1, -1, -1, -1, 1)
            check(Long.MIN_VALUE, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1)
        }
        with(DoubleBinaryEncoder()) {
            check(123.456, 64, 94, -35, 47, 26, -97, -66, 119)
            check(Double.POSITIVE_INFINITY, 127, -16, 0, 0, 0, 0, 0, 0)
            check(Double.NEGATIVE_INFINITY, -1, -16, 0, 0, 0, 0, 0, 0)
            check(Double.NaN, 127, -8, 0, 0, 0, 0, 0, 0)
        }
        @Suppress("SpellCheckingInspection")
        with(StringBinaryEncoder()) {
            check("", 0)
            check("abc", 3, 97, 98, 99)
            check("\u0000\u0001\u007F", 3, 0x00, 0x01, 0x7F)
            check("\u0080", 2, -62, 0x80)
            check("\u07FF", 2, -33, -65)
            check("\u0800", 3, -32, -96, 0x80)
            check("\uFFFF", 3, -17, -65, -65)
            check("\uD800\uDC00", 4, -16, -112, -128, -128) // U+010000
            check("\uD800\uDC01", 4, -16, -112, -128, -127) // U+010001
            check("\uDBFF\uDFFF", 4, -12, -113, -65, -65)   // U+1FFFFF
        }
        with(ByteArrayBinaryEncoder()) {
            check(byteArrayOf(), 0)
            check(byteArrayOf(0, 1, -1, 127, -128), 5, 0, 1, -1, 127, -128)
        }
        with(ColorEncoder) {
            check(Color.Red, 0)
            check(Color.Green, 1)
        }
    }

    @Test
    fun testOptional() {
        val writer = BytesWriter(1000)
        val encoder = OptionalStringEncoder()
        with(writer) {
            encoder.write(this, OptionalString(null))
            assertEquals(1, current)
            encoder.write(this, OptionalString("hello"))
            assertEquals(8, current)
        }
        with(BytesReader(writer.buffer)) {
            assertNull(encoder.read(this).s)
            assertEquals(1, current)
            assertEquals("hello", encoder.read(this).s)
            assertEquals(8, current)
        }
    }
}

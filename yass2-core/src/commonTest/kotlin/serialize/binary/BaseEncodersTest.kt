package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.transport.*
import kotlin.test.*

private enum class Color { Red, Green }

class BaseEncodersTest {
    @Test
    fun test() {
        fun <T : Any> BaseEncoder<T>.check(value: T, vararg bytes: Int) {
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
        with(BooleanEncoder) {
            check(false, 0)
            check(true, 1)
        }
        with(ByteEncoder) {
            check(0, 0)
            check(-1, -1)
            check(1, 1)
            check(Byte.MAX_VALUE, 127)
            check(Byte.MIN_VALUE, -128)
        }
        with(IntEncoder) {
            check(0, 0)
            check(-1, 1)
            check(1, 2)
            check(Int.MAX_VALUE, -2, -1, -1, -1, 15)
            check(Int.MIN_VALUE, -1, -1, -1, -1, 15)
        }
        with(LongEncoder) {
            check(0, 0)
            check(-1, 1)
            check(1, 2)
            check(Long.MAX_VALUE, -2, -1, -1, -1, -1, -1, -1, -1, -1, 1)
            check(Long.MIN_VALUE, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1)
        }
        with(DoubleEncoder) {
            check(123.456, 64, 94, -35, 47, 26, -97, -66, 119)
            check(Double.POSITIVE_INFINITY, 127, -16, 0, 0, 0, 0, 0, 0)
            check(Double.NEGATIVE_INFINITY, -1, -16, 0, 0, 0, 0, 0, 0)
            check(Double.NaN, 127, -8, 0, 0, 0, 0, 0, 0)
        }
        @Suppress("SpellCheckingInspection")
        with(StringEncoder) {
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
        with(ByteArrayEncoder) {
            check(byteArrayOf(), 0)
            check(byteArrayOf(0, 1, -1, 127, -128), 5, 0, 1, -1, 127, -128)
        }
        with(enumEncoder<Color>()) {
            check(Color.Red, 0)
            check(Color.Green, 1)
        }
    }

    @Test
    fun testOptional() {
        val writer = BytesWriter(1000)
        with(writer) {
            StringEncoder.writeOptional(this, null)
            assertEquals(1, current)
            StringEncoder.writeOptional(this, "hello")
            assertEquals(8, current)
        }
        with(BytesReader(writer.buffer)) {
            assertNull(StringEncoder.readOptional(this))
            assertEquals(1, current)
            assertEquals("hello", StringEncoder.readOptional(this))
            assertEquals(8, current)
        }
    }
}
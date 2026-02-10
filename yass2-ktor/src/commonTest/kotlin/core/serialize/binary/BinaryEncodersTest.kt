package ch.softappeal.yass2.core.serialize.binary

import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.core.assertFailsWithMessage
import ch.softappeal.yass2.core.serialize.ByteArrayReader
import ch.softappeal.yass2.core.serialize.ByteArrayWriter
import ch.softappeal.yass2.core.serialize.checkDrained
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

val GenderEncoder = EnumBinaryEncoder(Gender::class, enumValues())

private data class OptionalString(val s: String?)

private object OptionalStringEncoder : BinaryEncoder<OptionalString>(
    OptionalString::class,
    { value -> writeBinaryOptional(value.s) { StringBinaryEncoder.write(this, it) } },
    { OptionalString(readBinaryOptional { StringBinaryEncoder.read(this) }) }
)

private fun <T : Any> BinaryEncoder<T>.check(value: T, vararg bytes: Int) {
    val writer = ByteArrayWriter()
    with(writer) {
        write(this, value)
        assertEquals(bytes.map { it.toByte() }, toyByteArray().toList())
    }
    with(ByteArrayReader(writer.toyByteArray())) {
        if (value is ByteArray) {
            assertEquals(value.toList(), (read(this) as ByteArray).toList())
        } else {
            assertEquals(value, read(this))
        }
        checkDrained()
    }
}

class BinaryEncodersTest {
    @Test
    fun test() {
        with(BooleanBinaryEncoder) {
            check(false, 0)
            check(true, 1)
        }
        with(IntBinaryEncoder) {
            check(0, 0)
            check(-1, 1)
            check(1, 2)
            check(Int.MAX_VALUE, -2, -1, -1, -1, 15)
            check(Int.MIN_VALUE, -1, -1, -1, -1, 15)
        }
        @Suppress("SpellCheckingInspection")
        with(StringBinaryEncoder) {
            check("", 0)
            check("abc", 3, 97, 98, 99)
            check("\u0000\u0001\u007F", 3, 0x00, 0x01, 0x7F)
            check("\u0080", 2, -62, 0x80)
            check("\u07FF", 2, -33, -65)
            check("\u0800", 3, -32, -96, 0x80)
            check("\uFFFF", 3, -17, -65, -65)
            check("\uD800\uDC00", 4, -16, -112, -128, -128) // U+010000
            check("\uD800\uDC01", 4, -16, -112, -128, -127) // U+010001
            check("\uDBFF\uDFFF", 4, -12, -113, -65, -65)   // U+10FFFF
            assertFails { read(ByteArrayReader(byteArrayOf(1, -1))) }
        }
        with(ByteArrayBinaryEncoder) {
            check(byteArrayOf(), 0)
            check(byteArrayOf(0, 1, -1, 127, -128), 5, 0, 1, -1, 127, -128)
        }
        with(GenderEncoder) {
            check(Gender.Female, 0)
            check(Gender.Male, 1)
            assertFailsWithMessage<IllegalStateException>("illegal constant 2") { read(ByteArrayReader(byteArrayOf(2))) }
            assertFailsWithMessage<IllegalStateException>("illegal constant -1") {
                read(ByteArrayReader(byteArrayOf(-1, -1, -1, -1, 15)))
            }
        }
        with(OptionalStringEncoder) {
            check(OptionalString(null), 0)
            check(OptionalString("hello"), 1, 5, 104, 101, 108, 108, 111)
        }
    }
}

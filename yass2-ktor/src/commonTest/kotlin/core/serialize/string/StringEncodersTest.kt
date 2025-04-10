package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.core.assertFailsMessage
import ch.softappeal.yass2.core.contract.Gender
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

private fun <T : Any> BaseStringEncoder<T>.test(value: T, vararg results: String) {
    results.forEach { assertEquals(value, read(it)) }
    assertTrue(results.any { it == write(value) })
}

class StringEncodersTest {
    @Test
    fun gender() {
        with(EnumStringEncoder(Gender::class, Gender::valueOf)) {
            test(Gender.Female, "Female")
            test(Gender.Male, "Male")
            assertFails { read("Unknown") }
        }
    }

    @Test
    fun int() {
        with(IntStringEncoder) {
            test(0, "0")
            test(1, "1")
            test(-1, "-1")
            test(Int.MAX_VALUE, "2147483647")
            test(Int.MIN_VALUE, "-2147483648")
            assertFails { read("Unknown") }
            assertFails { read("4123456789") }
        }
    }

    @Test
    fun long() {
        with(LongStringEncoder) {
            test(0L, "0")
            test(1L, "1")
            test(-1L, "-1")
            test(Long.MAX_VALUE, "9223372036854775807")
            test(Long.MIN_VALUE, "-9223372036854775808")
            assertFails { read("Unknown") }
            assertFails { read("51515131515131515154") }
        }
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun byteArray() {
        fun test(value: ByteArray, result: String) {
            assertEquals(result, ByteArrayStringEncoder.write(value))
            assertContentEquals(value, ByteArrayStringEncoder.read(result))
        }
        test(byteArrayOf(), "")
        test(byteArrayOf(0), "AA==")
        test(byteArrayOf(0, 1), "AAE=")
        test(byteArrayOf(0, 1, 2), "AAEC")
        test(byteArrayOf(0, 1, 2, 3), "AAECAw==")
        assertFails { ByteArrayStringEncoder.read("AA==x") }
        assertFails { ByteArrayStringEncoder.read("*A==") }
        assertFails { ByteArrayStringEncoder.read("A*==") }
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun string() {
        println("\t\r\n\\\"")
        val serializer = TextSerializer(listOf())
        fun test(value: String, result: String, hexResult: String? = null) {
            val r = serializer.writeString(value)
            assertEquals(result, r)
            @OptIn(ExperimentalStdlibApi::class)
            if (hexResult != null) assertEquals(hexResult, r.encodeToByteArray(throwOnInvalidSequence = true).toHexString())
            assertEquals(value, serializer.readString(result))
        }
        test("", "\"\"")
        test(" hello world ", "\" hello world \"")
        test("\u0000\u0001\u007F", "\"\u0000\u0001\u007F\"", "2200017f22")
        test("\u0080", "\"\u0080\"", "22c28022")
        test("\u07FF", "\"\u07FF\"", "22dfbf22")
        test("\u0800", "\"\u0800\"", "22e0a08022")
        test("\uFFFF", "\"\uFFFF\"", "22efbfbf22")
        test("\uD800\uDC00", "\"\uD800\uDC00\"", "22f090808022") // U+010000
        test("\uD800\uDC01", "\"\uD800\uDC01\"", "22f090808122") // U+010001
        test("\uD800\uDFFF", "\"\uD800\uDFFF\"", "22f0908fbf22") // U+0103FF
        test("\uDBFF\uDFFF", "\"\uDBFF\uDFFF\"", "22f48fbfbf22") // U+10FFFF
        test("\"a", "\"\\\"a\"", "225c226122")
        test("\\b", "\"\\\\b\"")
        test("\ta", "\"\\ta\"", "225c746122")
        test("\na", "\"\\na\"", "225c6e6122")
        test("\ra", "\"\\ra\"", "225c726122")
        println(serializer.readString("\"a\tb\""))
        println(serializer.readString("\"c\nd\""))
        assertFailsMessage<IllegalStateException>("illegal escape with codePoint 97") { serializer.readString("\"\\a\"") }
        println(assertFails { serializer.readString("invalid") })
        println(assertFails { serializer.readString("\"a") })
    }

    @Test
    fun double() {
        with(DoubleStringEncoder) {
            test(123.456, "123.456")
            test(Double.POSITIVE_INFINITY, "Infinity")
            test(Double.NEGATIVE_INFINITY, "-Infinity")
            test(Double.NaN, "NaN")
            test(
                -9.87654321E-123,
                "-9.87654321E-123",
                "-9.87654321e-123",
            )
            test(
                9.87654321E123,
                "9.87654321E123",
                "9.87654321e123",
                "9.87654321E+123",
                "9.87654321e+123",
                "+9.87654321e+123",
            )
            test(
                Double.MAX_VALUE,
                "1.7976931348623157E308",
                "1.7976931348623157e308",
                "1.7976931348623157E+308",
                "1.7976931348623157e+308",
                "+1.7976931348623157e+308",
            )
            test(
                -Double.MAX_VALUE,
                "-1.7976931348623157E308",
                "-1.7976931348623157e308",
                "-1.7976931348623157E+308",
                "-1.7976931348623157e+308",
            )
            test(
                1E-300,
                "1.0E-300",
                "1.0e-300",
                "1E-300",
                "1e-300",
                "+1e-300",
            )
            test(
                -1E-300,
                "-1.0E-300",
                "-1.0e-300",
                "-1E-300",
                "-1e-300",
            )
            assertFails { read("invalidDouble") }
            assertFails { read("1.3e") }
            assertFails { read("++2") }
        }
    }

    /** @see DoubleStringEncoder */
    @Test
    fun doubleNotJsPlatform() {
        with(DoubleStringEncoder) {
            try {
                test(0.0, "0.0")
                test(1.0, "1.0")
                test(-1.0, "-1.0")
            } catch (ignore: AssertionError) {
                println(ignore)
            }
        }
    }
}

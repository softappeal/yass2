package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.Gender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

fun <T : Any> BaseUtf8Encoder<T>.test(value: T, vararg results: String) {
    results.forEach { assertEquals(value, read(it)) }
    assertTrue(results.any { it == write(value) })
}

class Utf8EncodersTest {
    @Test
    fun gender() {
        with(EnumUtf8Encoder(Gender::class, Gender::valueOf)) {
            test(Gender.Female, "Female")
            test(Gender.Male, "Male")
            assertFails { read("Unknown") }
        }
    }

    @Test
    fun boolean() {
        with(BooleanUtf8Encoder) {
            test(false, "false")
            test(true, "true")
            assertFails { read("True") }
        }
    }

    @Test
    fun int() {
        with(IntUtf8Encoder) {
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
        with(LongUtf8Encoder) {
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
            assertEquals(result, ByteArrayUtf8Encoder.write(value))
            assertTrue(value contentEquals ByteArrayUtf8Encoder.read(result))
        }
        test(byteArrayOf(), "")
        test(byteArrayOf(0), "AA==")
        test(byteArrayOf(0, 1), "AAE=")
        test(byteArrayOf(0, 1, 2), "AAEC")
        test(byteArrayOf(0, 1, 2, 3), "AAECAw==")
        assertFails { ByteArrayUtf8Encoder.read("AA==x") }
        assertFails { ByteArrayUtf8Encoder.read("*A==") }
        assertFails { ByteArrayUtf8Encoder.read("A*==") }
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
}

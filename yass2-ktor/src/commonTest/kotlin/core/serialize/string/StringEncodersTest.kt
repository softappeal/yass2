package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.assertFailsWithMessage
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

val GenderEncoder = EnumStringEncoder(Gender::class, Gender::valueOf)

private fun <T : Any> BaseStringEncoder<T>.check(value: T, vararg results: String) {
    results.forEach { assertEquals(value, read(it)) }
    assertTrue(results.any { it == write(value) })
}

class StringEncodersTest {
    @Test
    fun gender() {
        with(GenderEncoder) {
            check(Gender.Male, "Male")
            check(Gender.Female, "Female")
            assertFails { read("Unknown") }
        }
    }

    @Test
    fun int() {
        with(IntStringEncoder) {
            check(0, "0")
            check(1, "1")
            check(-1, "-1")
            check(Int.MAX_VALUE, "2147483647")
            check(Int.MIN_VALUE, "-2147483648")
            assertFails { read("Unknown") }
            assertFails { read("4123456789") }
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
            val r = serializer.toString(value)
            assertEquals(result, r)
            if (hexResult != null) assertEquals(hexResult, r.encodeToByteArray(throwOnInvalidSequence = true).toHexString())
            assertEquals(value, serializer.fromString(result))
        }
        test("", "\"\"")
        test("$", "\"$\"")
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
        println(serializer.fromString("\"a\tb\""))
        println(serializer.fromString("\"c\nd\""))
        assertFailsWithMessage<IllegalStateException>("illegal escape with codePoint 97") { serializer.fromString("\"\\a\"") }
        println(assertFails { serializer.fromString("invalid") })
        println(assertFails { serializer.fromString("\"a") })
    }
}

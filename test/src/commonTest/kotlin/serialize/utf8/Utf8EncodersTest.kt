package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.serialize.BytesWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

private val SERIALIZER = TextSerializer(
    listOf(
        BooleanUtf8Encoder,
        IntUtf8Encoder,
        LongUtf8Encoder,
        ByteArrayUtf8Encoder,
        EnumUtf8Encoder(Gender::class, Gender::valueOf),
    ),
    false,
)

private fun test(value: Any?, result: String, hexResult: String? = null) {
    assertEquals(result, SERIALIZER.writeString(value))
    if (hexResult != null) {
        with(BytesWriter(1000)) {
            SERIALIZER.write(this, value)
            @OptIn(ExperimentalStdlibApi::class)
            assertEquals(hexResult, buffer.copyOf(current).toHexString())
        }
    }
    val d = SERIALIZER.readString(result)
    if (value is ByteArray) assertTrue(value contentEquals (d as ByteArray)) else assertEquals(value, d)
}

class Utf8EncodersTest {
    @Suppress("SpellCheckingInspection")
    @Test
    fun test() {
        test(null, "*")

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
        println(SERIALIZER.readString("\"a\tb\""))
        println(SERIALIZER.readString("\"c\nd\""))
        assertFailsMessage<IllegalStateException>("illegal escape with codePoint 97") { SERIALIZER.readString("\"\\ab\"") }

        test(listOf<Int>(), "[]")
        test(listOf(null), "[*]")
        test(listOf(null, null), "[*,*]")
        test(listOf(null, listOf("")), "[*,[\"\"]]")

        test(false, "Boolean(false)")
        test(true, "Boolean(true)")

        test(0, "Int(0)")
        test(1, "Int(1)")
        test(-1, "Int(-1)")
        test(Int.MAX_VALUE, "Int(2147483647)")
        test(Int.MIN_VALUE, "Int(-2147483648)")

        test(0L, "Long(0)")
        test(1L, "Long(1)")
        test(-1L, "Long(-1)")
        test(Long.MAX_VALUE, "Long(9223372036854775807)")
        test(Long.MIN_VALUE, "Long(-9223372036854775808)")

        test(Gender.Female, "Gender(Female)")
        test(Gender.Male, "Gender(Male)")

        test(byteArrayOf(), "ByteArray()")
        test(byteArrayOf(0), "ByteArray(AA==)")
        test(byteArrayOf(0, 1), "ByteArray(AAE=)")
        test(byteArrayOf(0, 1, 2), "ByteArray(AAEC)")
        test(byteArrayOf(0, 1, 2, 3), "ByteArray(AAECAw==)")

        listOf(
            "invalid",
            "\"a",
            "Boolean(1",
            "Boolean(True)",
            "Gender(Unknown)",
            "Int(Unknown)",
            "Int(4123456789)",
            "Long(Unknown)",
            "Long(51515131515131515154)",
            "ByteArray(AA==x)",
            "ByteArray(*A==)",
            "ByteArray(A*==)",
        ).forEach {
            println(
                assertFails { SERIALIZER.readString(it) }
            )
        }
    }
}

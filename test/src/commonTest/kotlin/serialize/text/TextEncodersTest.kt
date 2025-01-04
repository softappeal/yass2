package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.serialize.BytesWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextEncodersTest {
    @Suppress("SpellCheckingInspection")
    @Test
    fun test() {
        val serializer = object : TextSerializer() {
            init {
                initialize(
                    BooleanTextEncoder(),
                    IntTextEncoder(),
                    LongTextEncoder(),
                    DoubleTextEncoder(),
                    ByteArrayTextEncoder(),
                    EnumTextEncoder(Gender::class, Gender::valueOf),
                )
            }
        }

        fun test(value: Any?, result: String, hexResult: String? = null) {
            assertEquals(result, serializer.writeString(value))
            if (hexResult != null) {
                with(BytesWriter(1000)) {
                    serializer.write(this, value)
                    @OptIn(ExperimentalStdlibApi::class)
                    assertEquals(hexResult, buffer.copyOf(current).toHexString())
                }
            }
            val d = serializer.readString(result)
            if (value is ByteArray) assertTrue(value contentEquals (d as ByteArray)) else assertEquals(value, d)
        }

        test(null, "*")

        test("", "\"\"")
        test("hello", "\"hello\"")
        test("\u0000\u0001\u007F", "\"\u0000\u0001\u007F\"", "2200017f22")
        test("\u0080", "\"\u0080\"", "22c28022")
        test("\u07FF", "\"\u07FF\"", "22dfbf22")
        test("\u0800", "\"\u0800\"", "22e0a08022")
        test("\uFFFF", "\"\uFFFF\"", "22efbfbf22")
        test("\uD800\uDC00", "\"\uD800\uDC00\"", "22f090808022") // U+010000
        test("\uD800\uDC01", "\"\uD800\uDC01\"", "22f090808122") // U+010001
        test("\uD800\uDFFF", "\"\uD800\uDFFF\"", "22f0908fbf22") // U+0103FF
        test("\uDBFF\uDFFF", "\"\uDBFF\uDFFF\"", "22f48fbfbf22") // U+10FFFF
        test("\"", "\"\\\"\"")
        test("\\", "\"\\\\\"")
        assertFailsMessage<IllegalStateException>("illegal escape with codePoint 97") { serializer.readString("\"\\a\"") }

        test(listOf<Int>(), "[]")
        test(listOf(null), "[*]")
        test(listOf(null, null), "[*,*]")
        test(listOf(null, listOf("")), "[*,[\"\"]]")

        test(false, "Boolean(false)")
        test(true, "Boolean(true)")

        test(123, "Int(123)")
        test(-123, "Int(-123)")

        test(123L, "Long(123)")
        test(-123L, "Long(-123)")

        test(0.0, "Double(0.0)")
        test(123.0, "Double(123.0)")
        test(-123.0, "Double(-123.0)")
        test(Double.POSITIVE_INFINITY, "Double(Infinity)")
        test(Double.NEGATIVE_INFINITY, "Double(-Infinity)")
        test(Double.NaN, "Double(NaN)")

        test(Gender.Female, "Gender(Female)")
        test(Gender.Male, "Gender(Male)")

        test(byteArrayOf(), "ByteArray()")
        test(byteArrayOf(0), "ByteArray(AA==)")
        test(byteArrayOf(0, 1), "ByteArray(AAE=)")
        test(byteArrayOf(0, 1, 2), "ByteArray(AAEC)")
        test(byteArrayOf(0, 1, 2, 3), "ByteArray(AAECAw==)")
    }
}

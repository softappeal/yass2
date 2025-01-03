package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.contract.Gender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextEncodersTest {
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

        fun test(value: Any?, result: String) {
            assertEquals(result, serializer.writeString(value))
            val d = serializer.readString(result)
            if (value is ByteArray) assertTrue(value contentEquals (d as ByteArray)) else assertEquals(value, d)
        }

        test(null, "*")

        test("", "\"\"")
        test("hello", "\"hello\"")

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

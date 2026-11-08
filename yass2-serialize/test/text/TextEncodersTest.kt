package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.serialize.Gender
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

private fun <T : Any> TextEncoder<T>.check(value: T, vararg results: String) {
    if (value is ByteArray) {
        assertContentEquals(value, readBase(results[0]) as ByteArray)
        assertEquals(results[0], writeBase(value))
    } else {
        results.forEach { assertEquals(value, readBase(it)) }
        assertTrue(results.any { it == writeBase(value) })
    }
}

class TextEncodersTest {
    @Test
    fun boolean() {
        with(BooleanTextEncoder) {
            check(false, "false")
            check(true, "true")
            assertFails { readBase("False") }
        }
    }

    @Test
    fun enum() {
        with(EnumTextEncoder(Gender::class, Gender::valueOf)) {
            check(Gender.Male, "Male")
            check(Gender.Female, "Female")
            assertFails { readBase("Unknown") }
        }
    }

    @Test
    fun int() {
        with(IntTextEncoder) {
            check(0, "0")
            check(1, "1")
            check(-1, "-1")
            check(Int.MAX_VALUE, "2147483647")
            check(Int.MIN_VALUE, "-2147483648")
            assertFails { readBase("Unknown") }
            assertFails { readBase("4123456789") }
        }
    }

    @Test
    fun long() {
        with(LongTextEncoder) {
            check(0L, "0")
            check(1L, "1")
            check(-1L, "-1")
            check(Long.MAX_VALUE, "9223372036854775807")
            check(Long.MIN_VALUE, "-9223372036854775808")
            assertFails { readBase("Unknown") }
            assertFails { readBase("51515131515131515154") }
        }
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun byteArray() {
        with(ByteArrayTextEncoder) {
            check(byteArrayOf(), "")
            check(byteArrayOf(0), "AA==")
            check(byteArrayOf(0, 1), "AAE=")
            check(byteArrayOf(0, 1, 2), "AAEC")
            check(byteArrayOf(0, 1, 2, 3), "AAECAw==")
            assertFails { readBase("AA==x") }
            assertFails { readBase("*A==") }
            assertFails { readBase("A*==") }
        }
    }

    @Test
    fun double() {
        with(DoubleTextEncoder) {
            check(123.456, "123.456")
            check(Double.POSITIVE_INFINITY, "Infinity")
            check(Double.NEGATIVE_INFINITY, "-Infinity")
            check(Double.NaN, "NaN")
            check(
                -9.87654321E-123,
                "-9.87654321E-123",
                "-9.87654321e-123",
            )
            check(
                9.87654321E123,
                "9.87654321E123",
                "9.87654321e123",
                "9.87654321E+123",
                "9.87654321e+123",
                "+9.87654321e+123",
            )
            check(
                Double.MAX_VALUE,
                "1.7976931348623157E308",
                "1.7976931348623157e308",
                "1.7976931348623157E+308",
                "1.7976931348623157e+308",
                "+1.7976931348623157e+308",
            )
            check(
                -Double.MAX_VALUE,
                "-1.7976931348623157E308",
                "-1.7976931348623157e308",
                "-1.7976931348623157E+308",
                "-1.7976931348623157e+308",
            )
            check(
                1E-300,
                "1.0E-300",
                "1.0e-300",
                "1E-300",
                "1e-300",
                "+1e-300",
            )
            check(
                -1E-300,
                "-1.0E-300",
                "-1.0e-300",
                "-1E-300",
                "-1e-300",
            )
            assertFails { readBase("invalidDouble") }
            assertFails { readBase("1.3e") }
            assertFails { readBase("++2") }
        }
    }
}

abstract class DoubleTextEncodersTest {
    /** see [DoubleTextEncoder] */
    @Test
    fun double() {
        with(DoubleTextEncoder) {
            check(0.0, "0.0")
            check(1.0, "1.0")
            check(-1.0, "-1.0")
        }
    }
}

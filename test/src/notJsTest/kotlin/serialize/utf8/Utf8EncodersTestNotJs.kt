package ch.softappeal.yass2.serialize.utf8

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val SERIALIZER = TextSerializer(
    listOf(
        DoubleUtf8Encoder,
    ),
    false,
)

private fun test(value: Any, vararg results: String) {
    println(value)
    results.forEach { assertEquals(value, SERIALIZER.readString(it)) }
    assertTrue(results.any { it == SERIALIZER.writeString(value) })
}

class Utf8EncodersTestNotJs {
    @Test
    fun double() {
        test(0.0, "Double(0.0)")
        test(1.0, "Double(1.0)")
        test(-1.0, "Double(-1.0)")
        test(123.456, "Double(123.456)")
        test(Double.POSITIVE_INFINITY, "Double(Infinity)")
        test(Double.NEGATIVE_INFINITY, "Double(-Infinity)")
        test(Double.NaN, "Double(NaN)")
        test(
            9.87654321E123,
            "Double(9.87654321E123)",
            "Double(9.87654321e123)",
            "Double(9.87654321E+123)",
            "Double(9.87654321e+123)",
            "Double(+9.87654321e+123)",
        )
        test(
            -9.87654321E-123,
            "Double(-9.87654321E-123)",
            "Double(-9.87654321e-123)",
        )
        test(
            Double.MAX_VALUE,
            "Double(1.7976931348623157E308)",
            "Double(1.7976931348623157e308)",
            "Double(1.7976931348623157E+308)",
            "Double(1.7976931348623157e+308)",
            "Double(+1.7976931348623157e+308)",
        )
        test(
            -Double.MAX_VALUE,
            "Double(-1.7976931348623157E308)",
            "Double(-1.7976931348623157e308)",
            "Double(-1.7976931348623157E+308)",
            "Double(-1.7976931348623157e+308)",
        )
        test(
            1E-300,
            "Double(1.0E-300)",
            "Double(1.0e-300)",
            "Double(1E-300)",
            "Double(1e-300)",
            "Double(+1e-300)",
        )
        test(
            -1E-300,
            "Double(-1.0E-300)",
            "Double(-1.0e-300)",
            "Double(-1E-300)",
            "Double(-1e-300)",
        )
    }
}

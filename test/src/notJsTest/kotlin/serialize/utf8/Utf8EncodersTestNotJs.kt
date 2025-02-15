package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.NotJs
import kotlin.test.Test
import kotlin.test.assertFails

class Utf8EncodersTestNotJs {
    @Test
    fun double() {
        @OptIn(NotJs::class)
        with(DoubleUtf8Encoder) {
            test(0.0, "0.0")
            test(1.0, "1.0")
            test(-1.0, "-1.0")
            test(123.456, "123.456")
            test(Double.POSITIVE_INFINITY, "Infinity")
            test(Double.NEGATIVE_INFINITY, "-Infinity")
            test(Double.NaN, "NaN")
            test(
                9.87654321E123,
                "9.87654321E123",
                "9.87654321e123",
                "9.87654321E+123",
                "9.87654321e+123",
                "+9.87654321e+123",
            )
            test(
                -9.87654321E-123,
                "-9.87654321E-123",
                "-9.87654321e-123",
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
}

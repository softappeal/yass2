package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.contract.A
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Poly
import kotlin.test.Test

private fun Int(s: String) = IntUtf8Encoder.read(s)
private fun ByteArray(s: String) = ByteArrayUtf8Encoder.read(s)
private fun Null() = null

// spezial hack boolean, int, long, double, enum und bytearray
// fuer andere basetype: to String
// special handling for MutableList
// special handling for null?

class KotlinSerializerTest {
    @Test
    fun test() {
        val x = listOf(
            Gender.Male,
            true,
            false,
            //ByteArray("xxx"),
            123,
            123.321,
            Int("123"),
            "hello",
            listOf(
                Int("1"),
                2,
                3,
            ),
            Poly(
                a = A(
                    a = 1,
                ),
                b = B(
                    a = 1,
                    b = 2,
                ),
            ),
            Poly(
                a = B(
                    a = 10,
                    b = 20,
                ),
                b = B(
                    a = 1,
                    b = 2,
                ),
            ),
            ManyProperties(
                h = 8,
                d = 4,
                f = 6,
                g = 7,
                b = 2,
            ).apply {
                a = 1
                c = 3
                e = 5
                i = 9
                j = 10
            },
            Lists(
                list = listOf(
                    1,
                    2,
                ),
                listOptional = Null(),
                mutableList = mutableListOf(
                    3,
                    4,
                ),
                mutableListOptional = null,
            ),
        )
        println(x)
    }
}

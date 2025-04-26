package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.StringEncoders
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.core.assertFailsMessage
import ch.softappeal.yass2.core.serialize.BytesWriter
import ch.softappeal.yass2.core.serialize.toBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

fun StringSerializer.dump(value: Any?, serialized: String, vararg others: String) {
    fun write(data: Any?) {
        assertEquals(serialized, toString(data))
    }
    write(value)
    write(fromString(serialized))
    others.forEach { write(fromString(it)) }
}

private class Int
private object MyIntEncoder : StringEncoder<Int>(Int::class, {}, { Int() })

private val SERIALIZER = TextSerializer(StringEncoders)

private fun dump(value: Any?, serialized: String, vararg others: String) = SERIALIZER.dump(value, serialized, *others)

class TextSerializerTest {
    @Test
    fun checkBaseString() {
        fun checkBaseString(string: String, message: String) {
            assertFailsMessage<IllegalStateException>(message) {
                TextSerializer(
                    listOf(
                        object : BaseStringEncoder<Long>(
                            Long::class,
                            { string },
                            { 0L }
                        ) {},
                    )
                ).toBytes(0L)
            }
        }
        checkBaseString(" ", "' ' must not contain whitespace, '\"', ',' or ')'")
        checkBaseString("\"", "'\"' must not contain whitespace, '\"', ',' or ')'")
        checkBaseString(",", "',' must not contain whitespace, '\"', ',' or ')'")
        checkBaseString(")", "')' must not contain whitespace, '\"', ',' or ')'")
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun allBaseTypes() {
        SERIALIZER.allBaseTypesTest(
            """
                Types(
                    boolean: true
                    int: 1
                    long: 2
                    double: 123.456
                    string: "hello"
                    bytes: AAEC
                    gender: Female
                    list: [
                        null
                        false
                        true
                        Int(-1)
                        Long(2)
                        Double(123.456)
                        "hello"
                        ByteArray(AAEC)
                        Gender(Male)
                        [
                            Int(1)
                            [
                                "hello"
                                "world"
                            ]
                        ]
                        Types(
                            boolean: true
                            int: -123456
                            long: 9223372036854775807
                            double: 123.456
                            string: "hello"
                            bytes: AAEC
                            gender: Female
                            list: [
                            ]
                            b: B(
                                a: 1
                                b: 2
                            )
                        )
                        DivideByZeroException(
                        )
                        BodyProperty(
                            body: BodyProperty(
                                body: BodyProperty(
                                )
                            )
                        )
                        Poly(
                            a: B(
                                a: 10
                                b: 20
                            )
                            b: B(
                                a: 1
                                b: 2
                            )
                        )
                        ManyProperties(
                            h: 8
                            d: 4
                            f: 6
                            g: 7
                            b: 2
                            a: 1
                            c: 3
                            e: 5
                            i: 9
                            j: 10
                        )
                    ]
                    b: B(
                        a: 10
                        b: 20
                    )
                    booleanOptional: true
                    intOptional: 1
                    longOptional: 2
                    doubleOptional: 123.456
                    stringOptional: "hello"
                    bytesOptional: AAEC
                    genderOptional: Female
                    listOptional: [
                        "hello"
                    ]
                    bOptional: B(
                        a: 30
                        b: 40
                    )
                )
            """.trimIndent()
        )
    }

    @Test
    fun test() {
        dump(
            null,
            "null",
            "  null",
        )
        dump(
            "hello",
            """"hello"""",
            """  "hello"""",
        )
        dump(
            false,
            "false",
            "  false",
        )
        dump(
            true,
            "true",
            "  true",
        )
        dump(
            listOf<Int>(),
            """
                [
                ]
            """.trimIndent(),
            "[]",
        )
        dump(
            listOf(null),
            """
                [
                    null
                ]
            """.trimIndent(),
            "[null]",
        )
        dump(
            Gender.Male,
            "Gender(Male)",
            "  Gender  (  Male  )",
        )
        dump(
            123,
            "Int(123)",
            "  Int  (  123  )",
        )
        dump(
            Poly(B(1, 2), B(3, 4)),
            """
                Poly(
                    a: B(
                        a: 1
                        b: 2
                    )
                    b: B(
                        a: 3
                        b: 4
                    )
                )
            """.trimIndent(),
            "Poly(a:B(a:1,b:2),b:B(a:3,b:4))",
            "Poly(a:B(a:1,b:2,),b:B(a:3,b:4,),)",
            "Poly(a:B(a:1,b:2)b:B(a:3,b:4))",
        )
        dump(
            ThrowableFake("hello", "world"),
            """
                ThrowableFake(
                    cause: "hello"
                    message: "world"
                )
            """.trimIndent(),
        )
        dump(
            ThrowableFake(null, "m"),
            """
                ThrowableFake(
                    message: "m"
                )
            """.trimIndent(),
        )
        dump(
            DivideByZeroException(),
            """
                DivideByZeroException(
                )
            """.trimIndent(),
        )
    }

    @Test
    fun properties() {
        assertNull((SERIALIZER.fromString("""ThrowableFake(message:"hello")""") as ThrowableFake).cause) // implicit null
        assertNull((SERIALIZER.fromString("""ThrowableFake(message:"hello",cause:null)""") as ThrowableFake).cause) // explicit null
        assertFailsMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.fromString("A(noSuchProperty:[])")
        }
        assertFailsMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.fromString("A(a:1,a:1)")
        }
        println(assertFailsWith<Exception> { // missing required property
            SERIALIZER.fromString("A()")
        })
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> {
            TextSerializer(listOf(IntStringEncoder, IntStringEncoder))
        }.message!!
        assertTrue(message.startsWith("duplicated type 'class "))
        assertTrue(message.endsWith("Int'"))
    }

    @Test
    fun duplicatedClassName() {
        assertFailsMessage<IllegalArgumentException>("duplicated className 'Int'") {
            TextSerializer(listOf(IntStringEncoder, MyIntEncoder))
        }
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> {
            SERIALIZER.write(BytesWriter(1000), TextSerializerTest())
        }.message!!
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.endsWith("TextSerializerTest'"))
    }

    @Test
    fun missingEncoder() {
        assertFailsMessage<IllegalStateException>("missing encoder for class 'X'") {
            SERIALIZER.fromString("X()")
        }
    }
}

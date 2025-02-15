package ch.softappeal.yass2.serialize.string

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.A
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.BodyProperty
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.IntException
import ch.softappeal.yass2.contract.IntWrapper
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Optionals
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.contract.Types
import ch.softappeal.yass2.contract.createStringEncoders
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.binary.ManyPropertiesConst
import ch.softappeal.yass2.serialize.writeBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

fun StringSerializer.dump(value: Any?, serialized: String, vararg others: String) {
    fun write(data: Any?) {
        assertEquals(serialized, writeString(data))
    }
    write(value)
    write(readString(serialized))
    others.forEach { write(readString(it)) }
}

fun checkString(string: String, message: String, createSerializer: (encoders: List<StringEncoder<*>>) -> StringSerializer) {
    assertFailsMessage<IllegalStateException>(message) {
        createSerializer(listOf(object : BaseStringEncoder<Boolean>(Boolean::class,
            { string },
            { false }
        ) {})).writeBytes(false)
    }
}

private class Int
private object MyIntEncoder : StringEncoder<Int>(Int::class, {}, { Int() })

private val SERIALIZER = TextSerializer(createStringEncoders())

private fun dump(value: Any?, serialized: String, vararg others: String) = SERIALIZER.dump(value, serialized, *others)

fun StringSerializer.typesTest() {
    val serialized = writeString(Types(
        true,
        1,
        2,
        "hello",
        byteArrayOf(1, 2, 3),
        Gender.Female,
    ))
    println(serialized)
    assertEquals(serialized, writeString(readString(serialized)))
}

@Suppress("SpellCheckingInspection")
fun StringSerializer.everythingTest() {
    val everything =
        listOf(
            null(),
            Boolean("true"),
            Int("1"),
            Long("2"),
            "hello",
            ByteArray("AAEC"),
            Gender.Female(),
            Types(
                boolean = true,
                int = 1,
                long = 2,
                string = "hello",
                bytes = ByteArray("AAEC"),
                gender = Gender.Female(),
            ),
            listOf(
                Int("1"),
            ),
            DivideByZeroException(
            ),
            BodyProperty(
            ).apply {
                body = BodyProperty(
                ).apply {
                    body = BodyProperty(
                    ).apply {
                        body = null()
                    }
                }
            },
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
                    Int("1"),
                ),
                listOptional = null(),
            ),
            ValueReply(
                value = ByteArray("AAEC"),
            ),
        )
    val serialized = writeString(everything)
    println(serialized)
    assertEquals(serialized, writeString(readString(serialized)))
}

class TextSerializerTest {
    @Test
    fun checkString() {
        fun checkString(string: String, message: String) = checkString(string, message) { TextSerializer(it) }
        checkString(" ", "' ' must not contain whitespace, ',' or ')'")
        checkString(")", "')' must not contain whitespace, ',' or ')'")
        checkString(",", "',' must not contain whitespace, ',' or ')'")
    }

    @Test
    fun test() {
        dump(
            null,
            "*",
            "  *",
        )
        dump(
            "hello",
            """"hello"""",
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
                    *
                ]
            """.trimIndent(),
            "[*]",
        )
        dump(
            listOf(null, null),
            """
                [
                    *
                    *
                ]
            """.trimIndent(),
            "[**]",
            "[* *]",
            "[*,*]",
            "[*,*,]",
        )
        dump(
            listOf(null, listOf("")),
            """
                [
                    *
                    [
                        ""
                    ]
                ]
            """.trimIndent(),
            "[*[\"\"]]",
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
        SERIALIZER.typesTest()
        dump(
            IntWrapper(3),
            """
                IntWrapper(
                    i: 3
                )
            """.trimIndent(),
            """
                IntWrapper(
                    i : 3,
                )
            """.trimIndent(),
            "IntWrapper(i:3)",
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
            listOf(null, 123, A(1), listOf(1, 2)),
            """
                [
                    *
                    Int(123)
                    A(
                        a: 1
                    )
                    [
                        Int(1)
                        Int(2)
                    ]
                ]
            """.trimIndent(),
            "[*,Int(123),A(a:1),[Int(1),Int(2)]]",
            "[*,Int(123),A(a:1,),[Int(1),Int(2),],]",
            "[*Int(123)A(a:1)[Int(1)Int(2)]]",
            "  \t \r \n [  *  Int  (  123  )  A  (  a  :  1  )  [  Int  (  1  )  Int  (  2  )  ]  ]",
        )
        dump(
            Optionals(1, null, IntWrapper(3), null),
            """
                Optionals(
                    i: 1
                    intWrapper: IntWrapper(
                        i: 3
                    )
                )
            """.trimIndent(),
        )
        dump(
            Optionals(1, 2, IntWrapper(3), IntWrapper(4)),
            """
                Optionals(
                    i: 1
                    iOptional: 2
                    intWrapper: IntWrapper(
                        i: 3
                    )
                    intWrapperOptional: IntWrapper(
                        i: 4
                    )
                )
            """.trimIndent(),
        )
        dump(
            Lists(listOf(1, 2), listOf(3, 4)),
            """
                Lists(
                    list: [
                        Int(1)
                        Int(2)
                    ]
                    listOptional: [
                        Int(3)
                        Int(4)
                    ]
                )
            """.trimIndent(),
        )
        dump(
            Lists(listOf(1, 2), null),
            """
                Lists(
                    list: [
                        Int(1)
                        Int(2)
                    ]
                )
            """.trimIndent(),
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
            ManyPropertiesConst,
            """
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
            """.trimIndent(),
        )
        dump(
            IntException(10),
            """
                IntException(
                    i: 10
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
        SERIALIZER.readString("Optionals(i:1,intWrapper:IntWrapper(i:3))") // implicit optional properties iOptional and intWrapperOptional
        assertFailsMessage<IllegalStateException>("no property 'Optionals.noSuchProperty'") {
            SERIALIZER.readString("Optionals(noSuchProperty:[])")
        }
        assertFailsMessage<IllegalStateException>("property 'Optionals.intWrapperOptional' must not be explicitly set to null") {
            SERIALIZER.readString("Optionals(intWrapperOptional:*)")
        }
        assertFailsMessage<IllegalStateException>("duplicated property 'Optionals.i'") {
            SERIALIZER.readString("Optionals(i:1,i:1)")
        }
        println(assertFailsWith<Exception> { // missing required property intWrapper
            SERIALIZER.readString("Optionals(i:1)")
        })
        println(assertFailsWith<Exception> { // missing required property i
            SERIALIZER.readString("Optionals(intWrapper:IntWrapper(i:3))")
        })
        println(assertFailsWith<Exception> { // wrong typ of property intWrapper
            SERIALIZER.readString("Optionals(i:1,intWrapper:A(a:3))")
        })
        println(assertFailsWith<Exception> { // wrong typ of property intWrapper
            SERIALIZER.readString("Optionals(i:1,intWrapper:[])")
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
            SERIALIZER.write(BytesWriter(1000), 1.2)
        }.message!!
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.endsWith("Double'"))
    }

    @Test
    fun missingEncoder() {
        assertFailsMessage<IllegalStateException>("missing encoder for class 'X'") {
            SERIALIZER.readString("X()")
        }
    }

    @Test
    fun everything() {
        SERIALIZER.everythingTest()
    }
}

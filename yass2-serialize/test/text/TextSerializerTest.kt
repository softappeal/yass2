package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.serialize.A
import ch.softappeal.yass2.serialize.B
import ch.softappeal.yass2.serialize.Example
import ch.softappeal.yass2.serialize.Poly
import ch.softappeal.yass2.serialize.TextBuiltIn
import ch.softappeal.yass2.serialize.TextSerializer
import ch.softappeal.yass2.serialize.assertFailsWithMessage
import ch.softappeal.yass2.serialize.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal val SERIALIZER = TextSerializer(compact = false)

private fun TextSerializer.check(value: Any?, serialized: String, vararg others: String) {
    fun write(data: Any?) {
        assertEquals(serialized, toString(data))
    }
    write(value)
    write(fromString(serialized))
    others.forEach { write(fromString(it)) }
}

private fun check(value: Any?, serialized: String, vararg others: String) = SERIALIZER.check(value, serialized, *others)

class TextSerializerTest {
    @Suppress("SpellCheckingInspection")
    @Test
    fun builtIn() {
        fun fail(string: String) {
            println(">$string<")
            println(assertFails { SERIALIZER.fromString(string) })
            println()
        }
        fail("\"hello")
        fail("[")
        fail("[x")

        check(
            null,
            "*",
            "  *",
        )

        check(
            listOf<Int>(),
            """
                [
                ]
            """.trimIndent(),
            "[]",
            "  [  ]",
        )
        check(
            listOf(
                null,
                "hello",
            ),
            """
                [
                    *
                    "hello"
                ]
            """.trimIndent(),
            """[*"hello"]""",
            """  [  *  "hello"  ]""",
        )

        check(
            "hello",
            """"hello"""",
            "  \t\n\r \"hello\"",
        )
        check(
            " \" \\ ",
            """" \" \\ """",
        )
        assertFailsWithMessage<IllegalStateException>("illegal escape with codePoint 36") {
            SERIALIZER.fromString("\"\\$\"")
        }
        check(
            "\u0000\u0001\u007F",
            "\"\u0000\u0001\u007F\"",
        )
        check(
            "\u0080",
            "\"\u0080\"",
        )
        check(
            "\u07FF",
            "\"\u07FF\"",
        )
        check(
            "\u0800",
            "\"\u0800\"",
        )
        check(
            "\uFFFF",
            "\"\uFFFF\"",
        )
        check(
            "\uD800\uDC00", // U+010000
            "\"\uD800\uDC00\"",
        )
        check(
            "\uD800\uDC01", // U+010001
            "\"\uD800\uDC01\"",
        )
        check(
            "\uDBFF\uDFFF", // U+10FFFF
            "\"\uDBFF\uDFFF\"",
        )
    }

    @Test
    fun example() {
        check(
            Example(
                int = 10,
                intOptional = null,
                any = 11,
                anyOptional = null,
                list = listOf(12, 13),
                listOptional = null,
            ),
            """
                Example(
                    int: 10
                    any: Int(11)
                    list: [
                        Int(12)
                        Int(13)
                    ]
                )
            """.trimIndent(),
            "Example(any:Int(11)list:[Int(12)Int(13)]int:10)",
            "  Example  (  int  :  10  any  :  Int  (  11  )  list  :  [  Int  (  12  )  Int  (  13  )  ]  )",
        )
        check(
            Example(
                int = 10,
                intOptional = 14,
                any = 11,
                anyOptional = 15,
                list = listOf(12, 13),
                listOptional = listOf(16, 17),
            ),
            """
                Example(
                    int: 10
                    intOptional: 14
                    any: Int(11)
                    anyOptional: Int(15)
                    list: [
                        Int(12)
                        Int(13)
                    ]
                    listOptional: [
                        Int(16)
                        Int(17)
                    ]
                )
            """.trimIndent()
        )
    }

    @Test
    fun poly() {
        check(
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
            """
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
            """.trimIndent(),
        )
    }

    @Test
    fun textBuiltIn() {
        check(
            TextBuiltIn(
                string = "hello",
                stringOptional = null,
            ),
            """
                TextBuiltIn(
                    string: "hello"
                )
            """.trimIndent()
        )
        check(
            TextBuiltIn(
                string = "hello",
                stringOptional = "world",
            ),
            """
                TextBuiltIn(
                    string: "hello"
                    stringOptional: "world"
                )
            """.trimIndent()
        )
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> { TextSerializer(true, IntTextEncoder, IntTextEncoder) }.message!!
        println(message)
        assertTrue(message.startsWith("duplicated type 'class "))
        assertTrue(message.contains("Int"))
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> { SERIALIZER.toByteArray(TextSerializerTest()) }.message!!
        println(message)
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.contains("TextSerializerTest"))
    }

    @Test
    fun properties() {
        assertFailsWithMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.fromString("A(noSuchProperty:[])")
        }
        assertFailsWithMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.fromString("A(a:1 a:1)")
        }
        println(assertFails { // missing required property
            SERIALIZER.fromString("B()")
        })
    }

    @Test
    fun duplicatedClassName() {
        class Int
        assertFailsWithMessage<IllegalArgumentException>("duplicated className 'Int'") {
            TextSerializer(true, IntTextEncoder, ClassTextEncoder(Int::class, {}, { Int() }))
        }
    }

    @Test
    fun missingEncoder() {
        assertFailsWithMessage<IllegalStateException>("missing encoder for class 'X'") { SERIALIZER.fromString("X()") }
    }

    @Test
    fun write() {
        fun write(string: String) = TextSerializer(
            true,
            object : TextEncoder<Long>(
                Long::class,
                { string },
                { 0L }
            ) {},
        ).toByteArray(0L)
        write("123")

        fun check(string: String, message: String) {
            assertFailsWithMessage<IllegalStateException>(message) { write(string) }
        }
        check(" ", "' ' must not contain whitespace or ')'")
        check(")", "')' must not contain whitespace or ')'")
    }

    @Test
    fun read() {
        fun read(string: String) = SERIALIZER.fromString(string)
        read("Int(123)")
        read("Int(123 )")
    }

    @Test
    fun nullableProperties() {
        println(assertFails {
            SERIALIZER.fromString("A(a:*)")
        })
        assertNull((SERIALIZER.fromString("A()") as A).a)
    }

    @Test
    fun hackedProperty() {
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Poly(
                        a: B(
                            a: 10
                            b: 20
                        )
                        b: A()
                    )
                """.trimIndent()
            )
        })
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Poly(
                        a: B(
                            a: 10
                            b: 20
                        )
                        b: *
                    )
                """.trimIndent()
            )
        })
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Poly(
                        a: B(
                            a: 10
                            b: 20
                        )
                        b: ""
                    )
                """.trimIndent()
            )
        })
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Poly(
                        a: B(
                            a: 10
                            b: 20
                        )
                        b: []
                    )
                """.trimIndent()
            )
        })
    }

    @Test
    fun compact() {
        TextSerializer().check(
            listOf(
                null,
                1,
                listOf<Int>(),
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
                Example(
                    int = 10,
                    intOptional = null,
                    any = 11,
                    anyOptional = null,
                    list = listOf(13),
                    listOptional = null,
                ),
            ),
            "[ * Int(1) [ ] Poly( a:B( a:10 b:20 ) b:B( a:1 b:2 ) ) Example( int:10 any:Int(11) list:[ Int(13) ] ) ]",
            "[*Int(1)[]Poly(a:B(a:10 b:20)b:B(a:1 b:2))Example(int:10 any:Int(11)list:[Int(13)])]",
        )
    }
}

abstract class HackedTextSerializerTest {
    @Test
    fun hackedListSimulated() {
        fun test(element: Any?) {
            val list = listOf(element)
            @Suppress("UNCHECKED_CAST") val castedList = list as List<Int>
            println(assertFails {
                castedList.forEach {
                    @Suppress("USELESS_CAST")
                    it as Int
                }
            })
        }
        test(null)
        test("")
        test(A(1))
        test(listOf<Int>())
    }

    @Test
    fun hackedList() {
        SERIALIZER.fromString(
            """
                [
                    *
                    ""
                    A()
                ]
            """.trimIndent()
        )
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Example(
                        int: 10
                        any: Int(11)
                        list: [
                            *
                        ]
                    )
                """.trimIndent()
            )
        })
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Example(
                        int: 10
                        any: Int(11)
                        list: [
                            ""
                        ]
                    )
                """.trimIndent()
            )
        })
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Example(
                        int: 10
                        any: Int(11)
                        list: [
                            A()
                        ]
                    )
                """.trimIndent()
            )
        })
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Example(
                        int: 10
                        any: Int(11)
                        list: [
                            []
                        ]
                    )
                """.trimIndent()
            )
        })
    }
}

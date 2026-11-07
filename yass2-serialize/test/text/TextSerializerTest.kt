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

internal val SERIALIZER = TextSerializer()

private fun check(value: Any?, serialized: String, vararg others: String) {
    fun write(data: Any?) {
        assertEquals(serialized, SERIALIZER.toString(data))
    }
    write(value)
    write(SERIALIZER.fromString(serialized))
    others.forEach { write(SERIALIZER.fromString(it)) }
}

class TextSerializerTest {
    @Test
    fun builtIn() {
        fun fail(string: String) {
            println(">$string<")
            println(assertFails { SERIALIZER.fromString(string) })
            println()
        }
        fail("nullX")
        fail("null ")
        fail("falseX")
        fail("trueX")
        fail("\"hello")
        fail("[")

        check(
            null,
            "null",
            "  null",
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
        @Suppress("SpellCheckingInspection")
        check(
            listOf(
                null,
                false,
                true,
                "hello",
            ),
            """
                [
                    null
                    false
                    true
                    "hello"
                ]
            """.trimIndent(),
            """[nullfalsetrue"hello"]""",
            """  [  null  false  true  "hello"  ]""",
        )

        check(
            false,
            "false",
            "  false",
        )
        check(
            true,
            "true",
            "  true",
        )

        check(
            "hello",
            """"hello"""",
            "  \t\n\r \"hello\"",
        )
        check(
            " \" \\ \t \n \r ",
            "\" \\\" \\\\ \\t \\n \\r \"",
        )
        assertFailsWithMessage<IllegalStateException>("illegal escape with codePoint 36") {
            SERIALIZER.fromString("\"\\$\"")
        }
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
            """Example(int:10 any:Int(11)list:[Int(12)Int(13)])""",
            """  Example  (  int  :  10  any  :  Int  (  11  )  list  :  [  Int  (  12  )  Int  (  13  )  ]  )""",
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
            """.trimIndent()
        )
    }

    @Test
    fun textBuiltIn() {
        check(
            TextBuiltIn(
                boolean = true,
                booleanOptional = null,
                string = "hello",
                stringOptional = null,
            ),
            """
                TextBuiltIn(
                    boolean: true
                    string: "hello"
                )
            """.trimIndent()
        )
        check(
            TextBuiltIn(
                boolean = true,
                booleanOptional = false,
                string = "hello",
                stringOptional = "world",
            ),
            """
                TextBuiltIn(
                    boolean: true
                    booleanOptional: false
                    string: "hello"
                    stringOptional: "world"
                )
            """.trimIndent()
        )
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> { TextSerializer(IntTextEncoder, IntTextEncoder) }.message!!
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
            TextSerializer(IntTextEncoder, ClassTextEncoder(Int::class, {}, { Int() }))
        }
    }

    @Test
    fun missingEncoder() {
        assertFailsWithMessage<IllegalStateException>("missing encoder for class 'X'") { SERIALIZER.fromString("X()") }
    }

    @Test
    fun writeBase() {
        fun write(string: String) = TextSerializer(
            object : TextEncoder<Long>(
                Long::class,
                { string },
                { 0L }
            ) {},
        ).toByteArray(0L)
        write("123")

        fun checkBaseString(string: String, message: String) {
            assertFailsWithMessage<IllegalStateException>(message) { write(string) }
        }
        checkBaseString(" ", "' ' must not contain whitespace or ')'")
        checkBaseString(")", "')' must not contain whitespace or ')'")
    }

    @Test
    fun readBase() {
        fun read(string: String) = SERIALIZER.fromString(string)
        read("Int(123)")
        read("Int(123 )")
    }

    @Test
    fun nullableProperties() {
        // NOTE: nullable properties can't be set explicitly to null; they are null if omitted
        println(assertFails {
            SERIALIZER.fromString("A(a:null)")
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
    }
}

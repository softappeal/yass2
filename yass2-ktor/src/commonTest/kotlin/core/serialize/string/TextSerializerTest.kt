package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.Example
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.StringEncoders
import ch.softappeal.yass2.ThrowableFake
import ch.softappeal.yass2.assertFailsWithMessage
import ch.softappeal.yass2.core.serialize.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

fun StringSerializer.check(value: Any?, serialized: String, vararg others: String) {
    fun write(data: Any?) {
        assertEquals(serialized, toString(data))
    }
    write(value)
    write(fromString(serialized))
    others.forEach { write(fromString(it)) }
}

private val SERIALIZER = TextSerializer(StringEncoders)

private fun check(value: Any?, serialized: String, vararg others: String) = SERIALIZER.check(value, serialized, *others)

class TextSerializerTest {
    @Test
    fun checkBaseString() {
        fun checkBaseString(string: String, message: String) {
            assertFailsWithMessage<IllegalStateException>(message) {
                TextSerializer(
                    listOf(
                        object : BaseStringEncoder<Long>(
                            Long::class,
                            { string },
                            { 0L }
                        ) {},
                    )
                ).toByteArray(0L)
            }
        }
        checkBaseString(" ", "' ' must not contain whitespace, '\"', ',' or ')'")
        checkBaseString("\"", "'\"' must not contain whitespace, '\"', ',' or ')'")
        checkBaseString(",", "',' must not contain whitespace, '\"', ',' or ')'")
        checkBaseString(")", "')' must not contain whitespace, '\"', ',' or ')'")
    }

    @Test
    fun builtIn() {
        check(
            null,
            "null",
            "  null",
        )

        check(
            123,
            "Int(123)",
            "  Int  (  123  )",
        )

        check(
            listOf<Int>(),
            """
                [
                ]
            """.trimIndent(),
            "[]",
        )
        check(
            listOf(null, 60),
            """
                [
                    null
                    Int(60)
                ]
            """.trimIndent(),
            "[nullInt(60)]",
            "  [  nullInt  (  60  )  ,  ]",
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
            "\t\n\r \"hello\"",
        )

        check(
            Gender.Male,
            "Gender(Male)",
            "  Gender  (  Male  )",
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
            """Example(int:10,any:Int(11),list:[Int(12),Int(13),],)""",
            """  Example  (  int  :  10  ,  any  :  Int  (  11  )  ,  list  :  [  Int  (  12  )  ,  Int  (  13  )  ,  ]  ,  )""",
            """Example(int:10 any:Int(11)list:[Int(12)Int(13)])""",
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
    fun properties() {
        assertNull((SERIALIZER.fromString("""ThrowableFake(message:"hello")""") as ThrowableFake).cause) // implicit null
        assertNull((SERIALIZER.fromString("""ThrowableFake(message:"hello",cause:null)""") as ThrowableFake).cause) // explicit null
        assertFailsWithMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.fromString("A(noSuchProperty:[])")
        }
        assertFailsWithMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.fromString("A(a:1,a:1)")
        }
        println(assertFailsWith<Exception> { // missing required property
            SERIALIZER.fromString("A()")
        })
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> { TextSerializer(listOf(GenderEncoder, GenderEncoder)) }.message!!
        println(message)
        assertTrue(message.startsWith("duplicated type 'class "))
        assertTrue(message.endsWith("Gender'"))
    }

    @Test
    fun duplicatedClassName() {
        class Gender
        assertFailsWithMessage<IllegalArgumentException>("duplicated className 'Gender'") {
            TextSerializer(listOf(GenderEncoder, ClassStringEncoder(Gender::class, {}, { Gender() })))
        }
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> { SERIALIZER.toByteArray(TextSerializerTest()) }.message!!
        println(message)
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.endsWith("TextSerializerTest'"))
    }

    @Test
    fun missingEncoder() {
        assertFailsWithMessage<IllegalStateException>("missing encoder for class 'X'") { SERIALIZER.fromString("X()") }
    }
}

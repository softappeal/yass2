package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.Example
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.StringEncoders
import ch.softappeal.yass2.ThrowableFake
import ch.softappeal.yass2.core.assertFailsWithMessage
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private val SERIALIZER = JsonSerializer(StringEncoders)

private fun check(value: Any?, serialized: String, vararg others: String) = SERIALIZER.check(value, serialized, *others)

class JsonSerializerTest {
    @Test
    fun builtIn() {
        check(
            null,
            "null",
            "  null",
        )

        check(
            123,
            """{"#Int":"123"}""",
            """  {  "#Int"  :  "123"  }""",
        )

        check(
            listOf<Int>(),
            """
                [
                ]
            """.trimIndent(),
            "  [  ]",
        )
        check(
            listOf(null, 60),
            """
                [
                    null,
                    {"#Int":"60"}
                ]
            """.trimIndent(),
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
            """  "hello"""",
        )

        check(
            Gender.Male,
            """{"#Gender":"Male"}""",
            """  {  "#Gender"  :  "Male"  }""",
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
                {
                    "#": "Example",
                    "int": "10",
                    "any": {"#Int":"11"},
                    "list": [
                        {"#Int":"12"},
                        {"#Int":"13"}
                    ]
                }
            """.trimIndent(),
            """{"#":"Example","int":"10","any":{"#Int":"11"},"list":[{"#Int":"12"},{"#Int":"13"}]}""",
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
                {
                    "#": "Example",
                    "int": "10",
                    "intOptional": "14",
                    "any": {"#Int":"11"},
                    "anyOptional": {"#Int":"15"},
                    "list": [
                        {"#Int":"12"},
                        {"#Int":"13"}
                    ],
                    "listOptional": [
                        {"#Int":"16"},
                        {"#Int":"17"}
                    ]
                }
            """.trimIndent()
        )
    }

    @Test
    fun failed() {
        assertFailsWithMessage<IllegalStateException>("unexpected codePoint 120") { SERIALIZER.fromString("""x x""") }
        assertFailsWithMessage<IllegalStateException>("',' expected instead of ':'") { SERIALIZER.fromString("""{"#":"A","a":"1":""") }
        assertFailsWithMessage<IllegalStateException>("empty type") { SERIALIZER.fromString("""{"":""}""") }
        assertFailsWithMessage<IllegalStateException>("'#' expected") { SERIALIZER.fromString("""{"x":""}""") }
        assertFailsWithMessage<IllegalStateException>("'A' is ClassStringEncoder") { SERIALIZER.fromString("""{"#A":""}""") }
    }

    @Test
    fun properties() {
        assertNull((SERIALIZER.fromString("""{"#":"ThrowableFake","message":"hello"}""") as ThrowableFake).cause) // implicit null
        assertNull((SERIALIZER.fromString("""{"#":"ThrowableFake","message":"hello","cause":null}""") as ThrowableFake).cause) // explicit null
        assertFailsWithMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.fromString("""{"#":"A","noSuchProperty":[]}""")
        }
        assertFailsWithMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.fromString("""{"#":"A","a":"1","a":"1"}""")
        }
        println(assertFailsWith<Exception> { // missing required property
            SERIALIZER.fromString("""{"#":"A"}""")
        })
    }
}

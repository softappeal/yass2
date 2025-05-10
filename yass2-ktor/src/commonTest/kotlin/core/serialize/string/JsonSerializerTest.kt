package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.StringEncoders
import ch.softappeal.yass2.ThrowableFake
import ch.softappeal.yass2.core.assertFailsMessage
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private val SERIALIZER = JsonSerializer(StringEncoders)

private fun dump(value: Any?, serialized: String, vararg others: String) = SERIALIZER.dump(value, serialized, *others)

class JsonSerializerTest {
    @Suppress("SpellCheckingInspection")
    @Test
    fun allBaseTypes() {
        SERIALIZER.allBaseTypesTest(
            """
                {
                    "#": "Types",
                    "boolean": true,
                    "int": "1",
                    "long": "2",
                    "double": "123.456",
                    "string": "hello",
                    "bytes": "AAEC",
                    "gender": "Female",
                    "list": [
                        null,
                        false,
                        true,
                        {"#Int":"-1"},
                        {"#Long":"2"},
                        {"#Double":"123.456"},
                        "hello",
                        {"#ByteArray":"AAEC"},
                        {"#Gender":"Male"},
                        [
                            {"#Int":"1"},
                            [
                                "hello",
                                "world"
                            ]
                        ],
                        {
                            "#": "Types",
                            "boolean": true,
                            "int": "-123456",
                            "long": "9223372036854775807",
                            "double": "123.456",
                            "string": "hello",
                            "bytes": "AAEC",
                            "gender": "Female",
                            "list": [
                            ],
                            "b": {
                                "#": "B",
                                "a": "1",
                                "b": "2"
                            }
                        },
                        {
                            "#": "DivideByZeroException"
                        },
                        {
                            "#": "BodyProperty",
                            "body": {
                                "#": "BodyProperty",
                                "body": {
                                    "#": "BodyProperty"
                                }
                            }
                        },
                        {
                            "#": "Poly",
                            "a": {
                                "#": "B",
                                "a": "10",
                                "b": "20"
                            },
                            "b": {
                                "#": "B",
                                "a": "1",
                                "b": "2"
                            }
                        },
                        {
                            "#": "ManyProperties",
                            "h": "8",
                            "d": "4",
                            "f": "6",
                            "g": "7",
                            "b": "2",
                            "a": "1",
                            "c": "3",
                            "e": "5",
                            "i": "9",
                            "j": "10"
                        }
                    ],
                    "b": {
                        "#": "B",
                        "a": "10",
                        "b": "20"
                    },
                    "booleanOptional": true,
                    "intOptional": "1",
                    "longOptional": "2",
                    "doubleOptional": "123.456",
                    "stringOptional": "hello",
                    "bytesOptional": "AAEC",
                    "genderOptional": "Female",
                    "listOptional": [
                        "hello"
                    ],
                    "bOptional": {
                        "#": "B",
                        "a": "30",
                        "b": "40"
                    }
                }
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
            "  [  ]",
        )
        dump(
            listOf(null),
            """
                [
                    null
                ]
            """.trimIndent(),
        )
        dump(
            Gender.Male,
            """{"#Gender":"Male"}""",
            """  {  "#Gender"  :  "Male"  }""",
        )
        dump(
            123,
            """{"#Int":"123"}""",
            """  {  "#Int"  :  "123"  }""",
        )
        dump(
            ThrowableFake("hello", "world"),
            """
                {
                    "#": "ThrowableFake",
                    "cause": "hello",
                    "message": "world"
                }
            """.trimIndent(),
        )
        dump(
            ThrowableFake(null, "m"),
            """
                {
                    "#": "ThrowableFake",
                    "message": "m"
                }
            """.trimIndent(),
        )
        dump(
            DivideByZeroException(),
            """
                {
                    "#": "DivideByZeroException"
                }
            """.trimIndent(),
        )
    }

    @Test
    fun properties() {
        assertNull((SERIALIZER.fromString("""{"#":"ThrowableFake","message":"hello"}""") as ThrowableFake).cause) // implicit null
        assertNull((SERIALIZER.fromString("""{"#":"ThrowableFake","message":"hello","cause":null}""") as ThrowableFake).cause) // explicit null
        assertFailsMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.fromString("""{"#":"A","noSuchProperty":[]}""")
        }
        assertFailsMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.fromString("""{"#":"A","a":"1","a":"1"}""")
        }
        println(assertFailsWith<Exception> { // missing required property
            SERIALIZER.fromString("""{"#":"A"}""")
        })
    }
}

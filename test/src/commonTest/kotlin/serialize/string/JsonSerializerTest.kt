package ch.softappeal.yass2.serialize.string

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.contract.createStringEncoders
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private val SERIALIZER = JsonSerializer(createStringEncoders())

private fun dump(value: Any?, serialized: String, vararg others: String) = SERIALIZER.dump(value, serialized, *others)

class JsonSerializerTest {
    @Suppress("SpellCheckingInspection")
    @Test
    fun allBaseTypes() {
        SERIALIZER.allBaseTypesTest("""
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
        """.trimIndent())
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
        assertNull((SERIALIZER.readString("""{"#":"ThrowableFake","message":"hello"}""") as ThrowableFake).cause) // implicit null
        assertFailsMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.readString("""{"#":"A","noSuchProperty":[]}""")
        }
        assertFailsMessage<IllegalStateException>("property 'Types.bOptional' must not be explicitly set to null") {
            SERIALIZER.readString("""{"#":"Types","bOptional":null}""")
        }
        assertFailsMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.readString("""{"#":"A","a":"1","a":"1"}""")
        }
        println(assertFailsWith<Exception> { // missing required property
            SERIALIZER.readString("""{"#":"A"}""")
        })
    }
}

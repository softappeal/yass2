@file:Suppress("SpellCheckingInspection")

package ch.softappeal.yass2.serialize

import ch.softappeal.yass2.B
import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.ManyProperties
import ch.softappeal.yass2.Poly
import ch.softappeal.yass2.StringEncoders
import ch.softappeal.yass2.Types
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.string.ByteArray
import ch.softappeal.yass2.serialize.string.Int
import ch.softappeal.yass2.serialize.string.JsonSerializer
import ch.softappeal.yass2.serialize.string.KotlinSerializer
import ch.softappeal.yass2.serialize.string.StringSerializer
import ch.softappeal.yass2.serialize.string.TextSerializer
import ch.softappeal.yass2.serialize.string.fromString
import ch.softappeal.yass2.serialize.string.invoke
import ch.softappeal.yass2.serialize.string.toString
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private val AllBaseTypes =
    Types(
        boolean = false,
        int = 1,
        string = "hello",
        bytes = ByteArray("AAEC"),
        gender = Gender.Female(),
        list = listOf(
            null,
            false,
            true,
            Int("-1"),
            "hello",
            ByteArray("AAEC"),
            Gender.Male(),
            listOf(
                Int("1"),
                listOf(
                    "hello",
                    "world",
                ),
            ),
            Types(
                boolean = true,
                int = -123456,
                string = "hello",
                bytes = ByteArray("AAEC"),
                gender = Gender.Female(),
                list = listOf(
                ),
                b = B(
                    a = 1,
                    b = 2,
                ),
                booleanOptional = null,
                intOptional = null,
                stringOptional = null,
                bytesOptional = null,
                genderOptional = null,
                listOptional = null,
                bOptional = null,
            ),
            DivideByZeroException(
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
            ),
        ),
        b = B(
            a = 10,
            b = 20,
        ),
        booleanOptional = true,
        intOptional = 1,
        stringOptional = "hello",
        bytesOptional = ByteArray("AAEC"),
        genderOptional = Gender.Female(),
        listOptional = listOf(
            "hello",
        ),
        bOptional = B(
            a = 30,
            b = 40,
        ),
    )

private fun StringSerializer.check(serializedRaw: String) {
    val serialized = serializedRaw.trimIndent()
    assertEquals(serialized, toString(AllBaseTypes))
    assertEquals(serialized, toString(fromString(serialized)))
}

private fun BinarySerializer.check(serialized: ByteArray) {
    assertContentEquals(serialized, toByteArray(AllBaseTypes))
    assertContentEquals(serialized, toByteArray(fromByteArray(serialized)))
}

class SerializerTest {
    @Test
    fun kotlinSerializer() {
        KotlinSerializer(StringEncoders).check(
            """
                Types(
                    boolean = false,
                    int = 1,
                    string = "hello",
                    bytes = ByteArray("AAEC"),
                    gender = Gender.Female(),
                    list = listOf(
                        null,
                        false,
                        true,
                        Int("-1"),
                        "hello",
                        ByteArray("AAEC"),
                        Gender.Male(),
                        listOf(
                            Int("1"),
                            listOf(
                                "hello",
                                "world",
                            ),
                        ),
                        Types(
                            boolean = true,
                            int = -123456,
                            string = "hello",
                            bytes = ByteArray("AAEC"),
                            gender = Gender.Female(),
                            list = listOf(
                            ),
                            b = B(
                                a = 1,
                                b = 2,
                            ),
                            booleanOptional = null,
                            intOptional = null,
                            stringOptional = null,
                            bytesOptional = null,
                            genderOptional = null,
                            listOptional = null,
                            bOptional = null,
                        ),
                        DivideByZeroException(
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
                        ),
                    ),
                    b = B(
                        a = 10,
                        b = 20,
                    ),
                    booleanOptional = true,
                    intOptional = 1,
                    stringOptional = "hello",
                    bytesOptional = ByteArray("AAEC"),
                    genderOptional = Gender.Female(),
                    listOptional = listOf(
                        "hello",
                    ),
                    bOptional = B(
                        a = 30,
                        b = 40,
                    ),
                )
            """
        )
    }

    @Test
    fun textSerializer() {
        TextSerializer(StringEncoders).check(
            """
                Types(
                    boolean: false
                    int: 1
                    string: "hello"
                    bytes: AAEC
                    gender: Female
                    list: [
                        null
                        false
                        true
                        Int(-1)
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
                        )
                    ]
                    b: B(
                        a: 10
                        b: 20
                    )
                    booleanOptional: true
                    intOptional: 1
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
            """
        )
    }

    @Test
    fun jsonSerializer() {
        JsonSerializer(StringEncoders).check(
            """
                {
                    "#": "Types",
                    "boolean": false,
                    "int": "1",
                    "string": "hello",
                    "bytes": "AAEC",
                    "gender": "Female",
                    "list": [
                        null,
                        false,
                        true,
                        {"#Int":"-1"},
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
                            "b": "2"
                        }
                    ],
                    "b": {
                        "#": "B",
                        "a": "10",
                        "b": "20"
                    },
                    "booleanOptional": true,
                    "intOptional": "1",
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
            """
        )
    }

    @Test
    fun binarySerializer() {
        ch.softappeal.yass2.BinarySerializer.check(
            "13, 0, 2, 5, 104, 101, 108, 108, 111, 3, 0, 1, 2, 0, 12, 0, 2, 0, 2, 1, 3, 1, 4, 5, 104, 101, 108, 108, 111, 5, 3, 0, 1, 2, 6, 1, 1, 2, 3, 2, 1, 2, 4, 5, 104, 101, 108, 108, 111, 4, 5, 119, 111, 114, 108, 100, 13, 1, -1, -120, 15, 5, 104, 101, 108, 108, 111, 3, 0, 1, 2, 0, 0, 2, 4, 0, 0, 0, 0, 0, 0, 0, 11, 9, 8, 20, 40, 2, 4, 10, 16, 8, 12, 14, 4, 20, 40, 1, 1, 1, 2, 1, 5, 104, 101, 108, 108, 111, 1, 3, 0, 1, 2, 1, 0, 1, 1, 4, 5, 104, 101, 108, 108, 111, 1, 60, 80"
                .split(", ").map { it.toByte() }.toByteArray()
        )
    }
}

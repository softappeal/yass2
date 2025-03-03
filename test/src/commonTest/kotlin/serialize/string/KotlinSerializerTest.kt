package ch.softappeal.yass2.serialize.string

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.BodyProperty
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.contract.Types
import ch.softappeal.yass2.contract.createStringEncoders
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.readBytes
import ch.softappeal.yass2.serialize.writeBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private val SERIALIZER = KotlinSerializer(createStringEncoders())

private fun dump(value: Any?, serialized: String, vararg others: String) = SERIALIZER.dump(value, serialized, *others)

@Suppress("SpellCheckingInspection")
val AllBaseTypes =
    Types(
        boolean = true,
        int = 1,
        long = 2,
        double = 123.456,
        string = "hello",
        bytes = ByteArray("AAEC"),
        gender = Gender.Female(),
        list = listOf(
            null,
            false,
            true,
            Int("-1"),
            Long("2"),
            Double("123.456"),
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
                long = 9223372036854775807,
                double = 123.456,
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
                longOptional = null,
                doubleOptional = null,
                stringOptional = null,
                bytesOptional = null,
                genderOptional = null,
                listOptional = null,
                bOptional = null,
            ),
            DivideByZeroException(
            ),
            BodyProperty(
            ).apply {
                body = BodyProperty(
                ).apply {
                    body = BodyProperty(
                    ).apply {
                        body = null
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
        ),
        b = B(
            a = 10,
            b = 20,
        ),
        booleanOptional = true,
        intOptional = 1,
        longOptional = 2,
        doubleOptional = 123.456,
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

@Suppress("SpellCheckingInspection")
private val AllBaseTypesSerialized = """
    Types(
        boolean = true,
        int = 1,
        long = 2,
        double = 123.456,
        string = "hello",
        bytes = ByteArray("AAEC"),
        gender = Gender.Female(),
        list = listOf(
            null,
            false,
            true,
            Int("-1"),
            Long("2"),
            Double("123.456"),
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
                long = 9223372036854775807,
                double = 123.456,
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
                longOptional = null,
                doubleOptional = null,
                stringOptional = null,
                bytesOptional = null,
                genderOptional = null,
                listOptional = null,
                bOptional = null,
            ),
            DivideByZeroException(
            ),
            BodyProperty(
            ).apply {
                body = BodyProperty(
                ).apply {
                    body = BodyProperty(
                    ).apply {
                        body = null
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
        ),
        b = B(
            a = 10,
            b = 20,
        ),
        booleanOptional = true,
        intOptional = 1,
        longOptional = 2,
        doubleOptional = 123.456,
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
""".trimIndent()

fun Serializer.allBaseTypesAssert(serialized: ByteArray) {
    val allBaseTypes = readBytes(serialized) as Types
    assertEquals(15, allBaseTypes.list.size)
    assertContentEquals(writeBytes(AllBaseTypes), writeBytes(allBaseTypes))
}

fun StringSerializer.allBaseTypesTest(serialized: String) {
    assertEquals(serialized, writeString(AllBaseTypes))
    allBaseTypesAssert(serialized.encodeToByteArray(throwOnInvalidSequence = true))
}

class KotlinSerializerTest {
    @Test
    fun allBaseTypes() {
        SERIALIZER.allBaseTypesTest(AllBaseTypesSerialized)
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
                listOf(
                )
            """.trimIndent(),
            "listOf()"
        )
        dump(
            listOf(null),
            """
                listOf(
                    null,
                )
            """.trimIndent(),
            "listOf(null,)"
        )
        dump(
            Gender.Male(),
            "Gender.Male()",
            "  Gender  .  Male  (  )",
        )
        dump(
            Int("123"),
            """Int("123")""",
            """  Int  (  "123"  )""",
        )
        dump(
            ThrowableFake("hello", "world"),
            """
                ThrowableFake(
                    cause = "hello",
                    message = "world",
                )
            """.trimIndent(),
        )
        dump(
            ThrowableFake(null, "m"),
            """
                ThrowableFake(
                    cause = null,
                    message = "m",
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
        assertFailsMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.readString("A(noSuchProperty=1,)")
        }
        assertFailsMessage<IllegalStateException>("missing properties '[a, b]' for 'B'") {
            SERIALIZER.readString("B()")
        }
        assertFailsMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.readString("A(a=1,a=1,)")
        }
        assertFailsMessage<IllegalStateException>("duplicated property 'Types.stringOptional'") {
            SERIALIZER.readString("Types(stringOptional=null,stringOptional=null,)")
        }
    }
}

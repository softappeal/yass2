package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.B
import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.ManyProperties
import ch.softappeal.yass2.Poly
import ch.softappeal.yass2.StringEncoders
import ch.softappeal.yass2.ThrowableFake
import ch.softappeal.yass2.Types
import ch.softappeal.yass2.core.assertFailsMessage
import ch.softappeal.yass2.core.serialize.Serializer
import ch.softappeal.yass2.core.serialize.fromByteArray
import ch.softappeal.yass2.core.serialize.toByteArray
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private val SERIALIZER = KotlinSerializer(StringEncoders)

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
    val allBaseTypes = fromByteArray(serialized) as Types
    assertEquals(14, allBaseTypes.list.size)
    assertContentEquals(toByteArray(AllBaseTypes), toByteArray(allBaseTypes))
}

fun StringSerializer.allBaseTypesTest(serialized: String) {
    assertEquals(serialized, toString(AllBaseTypes))
    allBaseTypesAssert(serialized.encodeToByteArray(throwOnInvalidSequence = true))
}

class KotlinSerializerTest {
    @Test
    fun allBaseTypes() {
        SERIALIZER.allBaseTypesTest(AllBaseTypesSerialized)
    }

    @Suppress("CanConvertToMultiDollarString")
    @Test
    fun test() {
        dump(
            "a $ b",
            "\"a \\$ b\"",
        )
        dump(
            "a\$b",
            "\"a\\\$b\"",
        )
        dump(
            "a\${b",
            "\"a\\\${b\"",
        )
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
            SERIALIZER.fromString("A(noSuchProperty=1,)")
        }
        assertFailsMessage<IllegalStateException>("missing properties '[a, b]' for 'B'") {
            SERIALIZER.fromString("B()")
        }
        assertFailsMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.fromString("A(a=1,a=1,)")
        }
    }
}

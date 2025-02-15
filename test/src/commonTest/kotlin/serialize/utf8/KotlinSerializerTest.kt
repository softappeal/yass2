package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.NotJs
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.BodyProperty
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.Types
import ch.softappeal.yass2.contract.createUtf8Encoders
import ch.softappeal.yass2.remote.ValueReply
import kotlin.test.Test
import kotlin.test.assertEquals

private val SERIALIZER = KotlinSerializer(createUtf8Encoders())

private fun dump(value: Any?, serialized: String, vararg others: String) = SERIALIZER.dump(value, serialized, *others)

@Suppress("SpellCheckingInspection")
class KotlinSerializerTest {
    @Test
    fun test() {
        dump(
            Null(),
            "Null()",
            "  Null  (  )",
        )
        dump(
            "hello",
            """"hello"""",
            """  "hello"""",
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
                    Null(),
                )
            """.trimIndent(),
            "listOf(Null(),)"
        )
        dump(
            listOf(null, null),
            """
                listOf(
                    Null(),
                    Null(),
                )
            """.trimIndent(),
            "listOf(Null(),Null(),)"
        )
        dump(
            listOf(null, listOf("")),
            """
                listOf(
                    Null(),
                    listOf(
                        "",
                    ),
                )
                """.trimIndent(),
            """listOf(Null(),listOf("",),)"""
        )
        dump(
            Boolean("true"),
            """Boolean("true")""",
            """  Boolean  (  "true"  )""",
        )
        dump(
            Int("123"),
            """Int("123")""",
            """  Int  (  "123"  )""",
        )
        dump(
            Long("123"),
            """Long("123")""",
            """  Long  (  "123"  )""",
        )
        assertEquals(123.456, @OptIn(NotJs::class) Double("123.456")) // works on Js becaue 123.456 is a Double
        dump(
            ByteArray("AAEC"),
            """ByteArray("AAEC")""",
            """  ByteArray  (  "AAEC"  )""",
        )
        /*
        dump(
            Gender.Male,
            "Gender.Male",
            "  Gender  .  Male",
        )
         */
    }

    @Test
    fun everything() {
        val everything =
            listOf(
                Null(),
                Boolean("true"),
                Int("1"),
                Long("2"),
                "hello",
                ByteArray("AAEC"),
                Gender.Female,
                Types(
                    boolean = true,
                    int = 1,
                    long = 2,
                    string = "hello",
                    bytes = ByteArray("AAEC"),
                    gender = Gender.Female,
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
                            body = Null()
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
                    listOptional = Null(),
                ),
                ValueReply(
                    value = ByteArray("AAEC"),
                ),
            )
        val serialized = """
            listOf(
                Null(),
                Boolean("true"),
                Int("1"),
                Long("2"),
                "hello",
                ByteArray("AAEC"),
                Gender.Female,
                Types(
                    boolean = true,
                    int = 1,
                    long = 2,
                    string = "hello",
                    bytes = ByteArray("AAEC"),
                    gender = Gender.Female,
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
                            body = Null()
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
                    listOptional = Null(),
                ),
                ValueReply(
                    value = ByteArray("AAEC"),
                ),
            )
        """.trimIndent()
        assertEquals(serialized, SERIALIZER.writeString(everything))
        // assertEquals(serialized, SERIALIZER.writeString(SERIALIZER.readString(serialized)))
    }
}

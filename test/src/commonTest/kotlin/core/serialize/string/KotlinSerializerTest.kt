package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.Example
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.core.assertFailsWithMessage
import ch.softappeal.yass2.stringEncoders
import kotlin.test.Test

private val SERIALIZER = KotlinSerializer(stringEncoders())

private fun check(value: Any?, serialized: String, vararg others: String) = SERIALIZER.check(value, serialized, *others)

class KotlinSerializerTest {
    @Suppress("CanConvertToMultiDollarString")
    @Test
    fun builtIn() {
        check(
            null,
            "null",
            "  null",
        )

        check(
            Int("123"),
            """Int("123")""",
            """  Int  (  "123"  )""",
        )

        check(
            listOf<Int>(),
            """
                listOf(
                )
            """.trimIndent(),
            "listOf()"
        )
        check(
            listOf(null, 60),
            """
                listOf(
                    null,
                    Int("60"),
                )
            """.trimIndent(),
            """listOf(null,Int("60"),)"""
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
            "a $ b",
            "\"a \\$ b\"",
        )
        check(
            "a\$b",
            "\"a\\\$b\"",
        )
        check(
            "a\${b",
            "\"a\\\${b\"",
        )
        assertFailsWithMessage<IllegalStateException>("illegal escape with codePoint 120") {
            SERIALIZER.fromString("\"\\x\"")
        }
        check(
            "hello",
            """"hello"""",
            """  "hello"""",
        )

        check(
            Gender.Male(),
            "Gender.Male()",
            "  Gender  .  Male  (  )",
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
                    int = 10,
                    intOptional = null,
                    any = Int("11"),
                    anyOptional = null,
                    list = listOf(
                        Int("12"),
                        Int("13"),
                    ),
                    listOptional = null,
                )
            """.trimIndent(),
            """ Example(int=10,intOptional=null,any=Int("11"),anyOptional=null,list=listOf(Int("12"),Int("13"),),listOptional=null,)"""
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
                    int = 10,
                    intOptional = 14,
                    any = Int("11"),
                    anyOptional = Int("15"),
                    list = listOf(
                        Int("12"),
                        Int("13"),
                    ),
                    listOptional = listOf(
                        Int("16"),
                        Int("17"),
                    ),
                )
            """.trimIndent()
        )
    }

    @Test
    fun properties() {
        assertFailsWithMessage<IllegalStateException>("no property 'A.noSuchProperty'") {
            SERIALIZER.fromString("A(noSuchProperty=1,)")
        }
        assertFailsWithMessage<IllegalStateException>("missing properties [a, b] for 'B'") {
            SERIALIZER.fromString("B()")
        }
        assertFailsWithMessage<IllegalStateException>("duplicated property 'A.a'") {
            SERIALIZER.fromString("A(a=1,a=1,)")
        }
    }
}

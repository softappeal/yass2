package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.generate.*
import kotlin.reflect.full.*
import kotlin.test.*

class GenerateBinarySerializerTest {
    @Test
    fun enumClass() {
        assertEquals(
            "type 'class ch.softappeal.yass2.serialize.binary.Color' is enum",
            assertFailsWith<IllegalArgumentException> { generateBinarySerializer(listOf(), listOf(Color::class)) }.message
        )
    }

    @Test
    fun abstractClass() {
        assertEquals(
            "type 'class ch.softappeal.yass2.contract.Id' is abstract",
            assertFailsWith<IllegalArgumentException> { generateBinarySerializer(listOf(), listOf(Id::class)) }.message
        )
    }

    @Test
    fun notPropertyParameter() {
        class X(x: Int) {
            init {
                println(x)
            }
        }
        assertEquals(
            "primary constructor parameter 'x' of 'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$notPropertyParameter\$X' is not a property",
            assertFailsWith<IllegalArgumentException> { generateBinarySerializer(listOf(IntEncoder), listOf(X::class)) }.message
        )
    }

    @Test
    fun bodyPropertyNotVar() {
        class X {
            @Suppress("unused")
            val x: Int = 0
        }
        assertEquals(
            "body property 'x' of 'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$bodyPropertyNotVar\$X' is not 'var'",
            assertFailsWith<IllegalArgumentException> { generateBinarySerializer(listOf(IntEncoder), listOf(X::class)) }.message
        )
    }

    @Test
    fun noPrimaryConstructor() {
        @Suppress("unused", "ConvertSecondaryConstructorToPrimary")
        class X {
            constructor()
        }
        assertEquals(
            "'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$noPrimaryConstructor\$X' has no primary constructor",
            assertFailsWith<IllegalStateException> { generateBinarySerializer(listOf(IntEncoder), listOf(X::class)) }.message
        )
    }

    @Test
    @Suppress("unused")
    fun ignoreThrowableProperties() {
        class X(val cause: Int, val message: Int)
        class Y : Exception()
        class Z(val z: Int) : Exception()
        assertEquals(
            """
                @Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
                public fun binarySerializer(
                    baseEncoders: List<ch.softappeal.yass2.serialize.binary.BaseEncoder<*>>,
                ): ch.softappeal.yass2.serialize.binary.BinarySerializer =
                    ch.softappeal.yass2.serialize.binary.BinarySerializer(baseEncoders + listOf(
                        ch.softappeal.yass2.serialize.binary.ClassEncoder(null::class,
                            { w, i ->
                                w.writeNoIdRequired(2, i.cause)
                                w.writeNoIdRequired(2, i.message)
                            },
                            { r ->
                                val i = null(
                                    r.readNoIdRequired(2) as kotlin.Int,
                                    r.readNoIdRequired(2) as kotlin.Int,
                                )
                                i
                            }
                        ),
                        ch.softappeal.yass2.serialize.binary.ClassEncoder(null::class,
                            { _, _ -> },
                            {
                                val i = null(
                                )
                                i
                            }
                        ),
                        ch.softappeal.yass2.serialize.binary.ClassEncoder(null::class,
                            { w, i ->
                                w.writeNoIdRequired(2, i.z)
                            },
                            { r ->
                                val i = null(
                                    r.readNoIdRequired(2) as kotlin.Int,
                                )
                                i
                            }
                        ),
                    ))

            """.trimIndent(),
            generateBinarySerializer(listOf(IntEncoder), listOf(X::class, Y::class, Z::class))
        )
    }

    @Test
    fun duplicatedProperty() {
        open class X(private val y: Int) {
            @Suppress("unused")
            fun myPrivateY() = y
        }

        class Y(val y: Int) : X(y) {
            @Suppress("unused")
            fun myY() = y
        }
        assertEquals(1, X::class.memberProperties.size)
        assertEquals(1, Y::class.memberProperties.size)
    }
}

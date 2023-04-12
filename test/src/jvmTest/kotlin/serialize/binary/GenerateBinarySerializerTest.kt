package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.serialize.binary.reflect.*
import kotlin.reflect.full.*
import kotlin.test.*

private val NoBaseEncoders = listOf<BaseEncoder<*>>()
private val IntBaseEncoders = listOf(IntEncoder)
private val TwoIntBaseEncoders = listOf(IntEncoder, IntEncoder)

class GenerateBinarySerializerTest {
    @Test
    fun enumClass() = assertFailsMessage<IllegalArgumentException>("type 'class ch.softappeal.yass2.Color' is enum") {
        generateBinarySerializer(::NoBaseEncoders, listOf(Color::class))
    }

    @Test
    fun abstractClass() = assertFailsMessage<IllegalArgumentException>(("type 'class ch.softappeal.yass2.contract.Id' is abstract")) {
        generateBinarySerializer(::NoBaseEncoders, listOf(Id::class))
    }

    @Test
    fun duplicatedTypes() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        generateBinarySerializer(::IntBaseEncoders, listOf(Int::class))
    }

    @Test
    fun duplicatedBaseEncoder() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        generateBinarySerializer(::TwoIntBaseEncoders, listOf())
    }

    @Test
    fun duplicatedTreeConcreteClass() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        generateBinarySerializer(::NoBaseEncoders, listOf(Id2::class, Id2::class))
    }

    @Test
    fun duplicatedGraphConcreteClass() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        generateBinarySerializer(::NoBaseEncoders, listOf(), listOf(Node::class, Node::class))
    }

    @Test
    fun notPropertyParameter() {
        class X(x: Int) {
            init {
                println(x)
            }
        }
        assertFailsMessage<IllegalArgumentException>("primary constructor parameter 'x' of 'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$notPropertyParameter\$X' is not a property") {
            generateBinarySerializer(::IntBaseEncoders, listOf(X::class))
        }
    }

    @Test
    fun bodyPropertyNotVar() {
        class X {
            @Suppress("unused")
            val x: Int = 0
        }
        assertFailsMessage<IllegalArgumentException>("body property 'x' of 'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$bodyPropertyNotVar\$X' is not 'var'") {
            generateBinarySerializer(::IntBaseEncoders, listOf(X::class))
        }
    }

    @Test
    fun noPrimaryConstructor() {
        @Suppress("unused", "ConvertSecondaryConstructorToPrimary")
        class X {
            constructor()
        }
        assertFailsMessage<IllegalStateException>("'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$noPrimaryConstructor\$X' has no primary constructor") {
            generateBinarySerializer(::IntBaseEncoders, listOf(X::class))
        }
    }

    @Test
    @Suppress("unused")
    fun ignoreThrowableProperties() {
        class X(val cause: Int, val message: Int)
        class Y : Exception()
        class Z(val z: Int) : Exception()
        assertEquals(
            """
                @Suppress("RedundantSuppression", "UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
                public val GeneratedBinarySerializer: ch.softappeal.yass2.serialize.binary.BinarySerializer =
                    ch.softappeal.yass2.serialize.binary.BinarySerializer(ch.softappeal.yass2.serialize.binary.IntBaseEncoders + listOf(
                        ch.softappeal.yass2.serialize.binary.ClassEncoder(null::class, false,
                            { w, i ->
                                w.writeNoIdRequired(3, i.cause)
                                w.writeNoIdRequired(3, i.message)
                            },
                            { r ->
                                val i = null(
                                    r.readNoIdRequired(3) as kotlin.Int,
                                    r.readNoIdRequired(3) as kotlin.Int,
                                )
                                i
                            }
                        ),
                        ch.softappeal.yass2.serialize.binary.ClassEncoder(null::class, false,
                            { _, _ -> },
                            {
                                val i = null(
                                )
                                i
                            }
                        ),
                        ch.softappeal.yass2.serialize.binary.ClassEncoder(null::class, false,
                            { w, i ->
                                w.writeNoIdRequired(3, i.z)
                            },
                            { r ->
                                val i = null(
                                    r.readNoIdRequired(3) as kotlin.Int,
                                )
                                i
                            }
                        ),
                    ))

            """.trimIndent(),
            generateBinarySerializer(::IntBaseEncoders, listOf(X::class, Y::class, Z::class))
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

class ReflectionBinarySerializerTest : BinarySerializerTest() {
    override val serializer = reflectionBinarySerializer(BaseEncoders, TreeConcreteClasses, GraphConcreteClasses)

    @Test
    fun duplicatedInt() = assertFailsMessage<IllegalArgumentException>("duplicated type 'class kotlin.Int'") {
        reflectionBinarySerializer(listOf(IntEncoder), listOf(Int::class))
    }
}

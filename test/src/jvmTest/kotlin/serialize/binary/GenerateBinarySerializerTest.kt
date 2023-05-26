package ch.softappeal.yass2.serialize.binary

import kotlin.reflect.full.*
import kotlin.test.*

class GenerateBinarySerializerTest {
    /* TODO

private val NoBaseEncoderClasses = listOf<KClass<*>>()
private val IntBaseEncoderClasses = listOf(IntEncoder::class)
private val TwoIntBaseEncoderClasses = listOf(IntEncoder::class, IntEncoder::class)



    @Test
    fun enumClass() = assertFailsMessage<IllegalArgumentException>("type 'class ch.softappeal.yass2.Color' is enum") {
        StringBuilder().generateBinarySerializer(NoBaseEncoderClasses, listOf(Color::class))
    }

    @Test
    fun abstractClass() = assertFailsMessage<IllegalArgumentException>(("type 'class ch.softappeal.yass2.contract.Id' is abstract")) {
        StringBuilder().generateBinarySerializer(NoBaseEncoderClasses, listOf(Id::class))
    }

    @Test
    fun duplicatedTypes() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        StringBuilder().generateBinarySerializer(IntBaseEncoderClasses, listOf(Int::class))
    }

    @Test
    fun duplicatedBaseEncoder() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        StringBuilder().generateBinarySerializer(TwoIntBaseEncoderClasses, listOf())
    }

    @Test
    fun duplicatedTreeConcreteClass() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        StringBuilder().generateBinarySerializer(NoBaseEncoderClasses, listOf(Id2::class, Id2::class))
    }

    @Test
    fun duplicatedGraphConcreteClass() = assertFailsMessage<IllegalArgumentException>("duplicated types") {
        StringBuilder().generateBinarySerializer(NoBaseEncoderClasses, listOf(), listOf(Node::class, Node::class))
    }

    @Test
    fun notPropertyParameter() {
        class X(x: Int) {
            init {
                println(x)
            }
        }
        assertFailsMessage<IllegalArgumentException>("primary constructor parameter 'x' of 'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$notPropertyParameter\$X' is not a property") {
            StringBuilder().generateBinarySerializer(IntBaseEncoderClasses, listOf(X::class))
        }
    }

    @Test
    fun bodyPropertyNotVar() {
        class X {
            @Suppress("unused")
            val x: Int = 0
        }
        assertFailsMessage<IllegalArgumentException>("body property 'x' of 'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$bodyPropertyNotVar\$X' is not 'var'") {
            StringBuilder().generateBinarySerializer(IntBaseEncoderClasses, listOf(X::class))
        }
    }

    @Test
    fun noPrimaryConstructor() {
        @Suppress("unused", "ConvertSecondaryConstructorToPrimary")
        class X {
            constructor()
        }
        assertFailsMessage<IllegalStateException>("'class ch.softappeal.yass2.serialize.binary.GenerateBinarySerializerTest\$noPrimaryConstructor\$X' has no primary constructor") {
            StringBuilder().generateBinarySerializer(IntBaseEncoderClasses, listOf(X::class))
        }
    }

    @Test
    @Suppress("unused")
    fun ignoreThrowableProperties() {
        class X(val cause: Int, val message: Int)
        class Y : Exception()
        class Z(val z: Int) : Exception()

        val builder = StringBuilder()
        builder.generateBinarySerializer(IntBaseEncoderClasses, listOf(X::class, Y::class, Z::class))
        assertEquals(
            """

                public val GeneratedBinarySerializer: ch.softappeal.yass2.serialize.binary.BinarySerializer =
                    ch.softappeal.yass2.serialize.binary.BinarySerializer(listOf(
                        ch.softappeal.yass2.serialize.binary.IntEncoder(),
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
            builder.toString()
        )
    }
*/

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

package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.serialize.binary.reflect.*
import kotlin.reflect.full.*
import kotlin.test.*

private val ReflectionSerializer = reflectionBinarySerializer(BaseEncoders, ConcreteClasses)

class BinarySerializerReflectionTest : BinarySerializerGeneratedTest() {
    override val serializer = ReflectionSerializer

    @Test
    fun enumClass() {
        assertEquals(
            "type 'class ch.softappeal.yass2.serialize.binary.Color' is enum",
            assertFailsWith<IllegalArgumentException> { reflectionBinarySerializer(listOf(), listOf(Color::class)) }.message
        )
    }

    @Test
    fun abstractClass() {
        assertEquals(
            "type 'class ch.softappeal.yass2.contract.Id' is abstract",
            assertFailsWith<IllegalArgumentException> { reflectionBinarySerializer(listOf(), listOf(Id::class)) }.message
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
            "primary constructor parameter 'x' of 'class ch.softappeal.yass2.serialize.binary.BinarySerializerReflectionTest\$notPropertyParameter\$X' is not a property",
            assertFailsWith<IllegalArgumentException> { reflectionBinarySerializer(listOf(IntEncoder), listOf(X::class)) }.message
        )
    }

    @Test
    fun bodyPropertyNotVar() {
        class X {
            @Suppress("unused")
            val x: Int = 0
        }
        assertEquals(
            "body property 'x' of 'class ch.softappeal.yass2.serialize.binary.BinarySerializerReflectionTest\$bodyPropertyNotVar\$X' is not 'var'",
            assertFailsWith<IllegalArgumentException> { reflectionBinarySerializer(listOf(IntEncoder), listOf(X::class)) }.message
        )
    }

    @Test
    fun noPrimaryConstructor() {
        @Suppress("unused", "ConvertSecondaryConstructorToPrimary")
        class X {
            constructor()
        }
        assertEquals(
            "'class ch.softappeal.yass2.serialize.binary.BinarySerializerReflectionTest\$noPrimaryConstructor\$X' has no primary constructor",
            assertFailsWith<IllegalStateException> { reflectionBinarySerializer(listOf(IntEncoder), listOf(X::class)) }.message
        )
    }

    @Test
    @Suppress("unused")
    fun ignoreThrowableProperties() {
        class X(val cause: Int, val message: Int)
        class Y : Exception()
        class Z(val z: Int) : Exception()
        println(generateBinarySerializer(listOf(IntEncoder), listOf(X::class, Y::class, Z::class), "dummy"))
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

    @Test
    fun baseTypeInheritance() {
        abstract class A
        class B : A()
        class C : A()
        class X(val a: A)

        val bEncoder = BaseEncoder(B::class, { _, _ -> }, { B() })
        val serializer = reflectionBinarySerializer(listOf(bEncoder), listOf(C::class, X::class))
        assertTrue(serializer.copy(X(B()), intArrayOf(5, 3)).a is B)
        assertTrue(serializer.copy(X(C()), intArrayOf(5, 4)).a is C)
    }
}

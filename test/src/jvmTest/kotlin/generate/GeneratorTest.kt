package ch.softappeal.yass2.generate

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.serialize.binary.BaseEncoder
import ch.softappeal.yass2.serialize.binary.IntEncoder
import kotlin.reflect.KClass
import kotlin.test.Test

private class NotAnInterface

@Suppress("unused")
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

@Suppress("unused")
private class BodyPropertyNotVar {
    val x: Int = 0
}

@Suppress("unused", "ConvertSecondaryConstructorToPrimary")
private class NoPrimaryConstructor {
    constructor()
}

private interface NotRegularClass

private abstract class AbstractClass

private class ConstructorParameterIsNotProperty(@Suppress("UNUSED_PARAMETER") x: Int)

enum class Enum

private fun codeWriter() = CodeWriter(StringBuilder())

class GeneratorTest {
    @Test
    fun binarySerializer() {
        fun generateBinarySerializer(klass: KClass<*>) {
            codeWriter().generateBinarySerializer(emptyList(), listOf(klass), listOf())
        }
        assertFailsMessage<IllegalArgumentException>(
            "body property 'x' of 'ch.softappeal.yass2.generate.BodyPropertyNotVar' must be 'var'"
        ) { generateBinarySerializer(BodyPropertyNotVar::class) }
        assertFailsMessage<IllegalStateException>(
            "class 'ch.softappeal.yass2.generate.NoPrimaryConstructor' must hava a primary constructor"
        ) { generateBinarySerializer(NoPrimaryConstructor::class) }
        assertFailsMessage<IllegalArgumentException>(
            "primary constructor parameter 'x' of class 'ch.softappeal.yass2.generate.ConstructorParameterIsNotProperty' must be a property"
        ) { generateBinarySerializer(ConstructorParameterIsNotProperty::class) }
        assertFailsMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.NotRegularClass' must be concrete"
        ) { generateBinarySerializer(NotRegularClass::class) }
        assertFailsMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.AbstractClass' must be concrete"
        ) { generateBinarySerializer(AbstractClass::class) }
        fun generateBinarySerializer(
            baseEncoders: List<BaseEncoder<out Any>>,
            treeConcreteClasses: List<KClass<*>>,
            graphConcreteClasses: List<KClass<*>>,
        ) {
            codeWriter().generateBinarySerializer(baseEncoders, treeConcreteClasses, graphConcreteClasses)
        }
        assertFailsMessage<IllegalArgumentException>(
            "class must not be duplicated"
        ) { generateBinarySerializer(emptyList(), listOf(Int::class), listOf(Int::class)) }
        assertFailsMessage<IllegalArgumentException>(
            "class must not be duplicated"
        ) { generateBinarySerializer(listOf(IntEncoder), listOf(Int::class), listOf()) }
        assertFailsMessage<IllegalArgumentException>(
            "class must not be duplicated"
        ) { generateBinarySerializer(listOf(IntEncoder), listOf(), listOf(Int::class)) }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.Enum' belongs to 'baseEncoders'"
        ) { generateBinarySerializer(listOf(), listOf(Enum::class), listOf()) }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.Enum' belongs to 'baseEncoders'"
        ) { generateBinarySerializer(listOf(), listOf(), listOf(Enum::class)) }
    }

    @Test
    fun dumper() {
        assertFailsMessage<IllegalArgumentException>(
            "class must not be duplicated"
        ) { codeWriter().generateDumper(listOf(Int::class), listOf(Int::class)) }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.Enum' must not be specified"
        ) { codeWriter().generateDumper(listOf(Enum::class)) }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.Enum' must not be specified"
        ) { codeWriter().generateDumper(listOf(), listOf(Enum::class)) }
    }

    @Test
    fun proxy() {
        assertFailsMessage<IllegalArgumentException>(
            "'ch.softappeal.yass2.generate.NotAnInterface' must be an interface"
        ) { codeWriter().generateProxy(NotAnInterface::class) }
        assertFailsMessage<IllegalArgumentException>(
            "interface 'ch.softappeal.yass2.generate.Overloaded' must not overload functions"
        ) { codeWriter().generateProxy(Overloaded::class) }
    }
}

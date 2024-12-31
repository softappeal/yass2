package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import kotlin.reflect.KClass
import kotlin.test.Test

private class NotAnInterface

@Suppress("unused")
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

private class BodyPropertyNotVar {
    @Suppress("unused") val x: Int = 0
}

private class NoPrimaryConstructor {
    @Suppress("unused", "ConvertSecondaryConstructorToPrimary") constructor()
}

private interface NotRegularClass

private abstract class AbstractClass

private class ConstructorParameterIsNotProperty(@Suppress("UNUSED_PARAMETER") x: Int)

private enum class Enum { One }

private class MyEnumEncoder : BinaryEncoder<Enum>(Enum::class, {}, { Enum.One })

private fun codeWriter() = CodeWriter(StringBuilder())

class GeneratorTest {
    @Test
    fun binarySerializer() {
        fun generateBinarySerializer(klass: KClass<*>) {
            codeWriter().generateBinarySerializer(listOf(), listOf(), listOf(klass))
        }
        assertFailsMessage<IllegalArgumentException>(
            "body property x of ch.softappeal.yass2.generate.reflect.BodyPropertyNotVar must be var"
        ) { generateBinarySerializer(BodyPropertyNotVar::class) }
        assertFailsMessage<IllegalStateException>(
            "class ch.softappeal.yass2.generate.reflect.NoPrimaryConstructor must hava a primary constructor"
        ) { generateBinarySerializer(NoPrimaryConstructor::class) }
        assertFailsMessage<IllegalArgumentException>(
            "primary constructor parameter x of class ch.softappeal.yass2.generate.reflect.ConstructorParameterIsNotProperty must be a property"
        ) { generateBinarySerializer(ConstructorParameterIsNotProperty::class) }
        assertFailsMessage<IllegalArgumentException>(
            "class ch.softappeal.yass2.generate.reflect.NotRegularClass must be concrete"
        ) { generateBinarySerializer(NotRegularClass::class) }
        assertFailsMessage<IllegalArgumentException>(
            "class ch.softappeal.yass2.generate.reflect.AbstractClass must be concrete"
        ) { generateBinarySerializer(AbstractClass::class) }
        assertFailsMessage<IllegalStateException>(
            "enum class ch.softappeal.yass2.generate.reflect.Enum belongs to enumClasses"
        ) { generateBinarySerializer(Enum::class) }
        assertFailsMessage<IllegalStateException>(
            "enum class ch.softappeal.yass2.generate.reflect.Enum belongs to enumClasses"
        ) { codeWriter().generateBinarySerializer(listOf(), listOf(), listOf(Enum::class)) }
        assertFailsMessage<IllegalStateException>(
            "enum class ch.softappeal.yass2.generate.reflect.Enum belongs to enumClasses"
        ) { codeWriter().generateBinarySerializer(listOf(MyEnumEncoder::class), listOf(), listOf()) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [kotlin.Int] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(IntBinaryEncoder::class), listOf(), listOf(Int::class)) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [ch.softappeal.yass2.generate.reflect.Enum] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(), listOf(Enum::class, Enum::class), listOf()) }
    }

    @Test
    fun proxy() {
        assertFailsMessage<IllegalArgumentException>(
            "ch.softappeal.yass2.generate.reflect.NotAnInterface must be an interface"
        ) { codeWriter().generateProxy(NotAnInterface::class) }
        assertFailsMessage<IllegalArgumentException>(
            "interface ch.softappeal.yass2.generate.reflect.Overloaded has overloaded methods [f]"
        ) { codeWriter().generateProxy(Overloaded::class) }
    }
}

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.core.assertFailsMessage
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.generate.CodeWriter
import kotlin.reflect.KClass
import kotlin.test.Test

private class NotAnInterface

@Suppress("unused")
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

@Suppress("unused")
private interface NoSuspend {
    fun noSuspend()
}

private class BodyPropertyNotVar {
    @Suppress("unused") val x: Int = 0
}

private class NoPrimaryConstructor {
    @Suppress("unused", "ConvertSecondaryConstructorToPrimary") constructor()
}

private interface NotRegularClass

private abstract class AbstractClass

private class ConstructorParameterIsNotProperty(x: Int) {
    init {
        println(x)
    }
}

private enum class Enum { One }

private object MyEnumEncoder : BinaryEncoder<Enum>(Enum::class, {}, { Enum.One })

private fun codeWriter() = CodeWriter(StringBuilder())

class GeneratorTest {
    @Test
    fun binarySerializer() {
        fun generateBinarySerializer(klass: KClass<*>) {
            codeWriter().generateBinarySerializer(listOf(), listOf(klass))
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
        ) { codeWriter().generateBinarySerializer(listOf(MyEnumEncoder::class), listOf()) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [kotlin.Int] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(IntBinaryEncoder::class), listOf(Int::class)) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [ch.softappeal.yass2.generate.reflect.Enum] are duplicated"
        ) {
            codeWriter().generateBinarySerializer(listOf(), listOf(Enum::class, Enum::class))
        }
    }

    @Test
    fun proxy() {
        assertFailsMessage<IllegalArgumentException>(
            "ch.softappeal.yass2.generate.reflect.NotAnInterface must be an interface"
        ) { codeWriter().generateProxy(NotAnInterface::class) }
        assertFailsMessage<IllegalArgumentException>(
            "interface ch.softappeal.yass2.generate.reflect.Overloaded has overloaded methods [f]"
        ) { codeWriter().generateProxy(Overloaded::class) }
        assertFailsMessage<IllegalArgumentException>(
            "method ch.softappeal.yass2.generate.reflect.NoSuspend.noSuspend must be suspend"
        ) { codeWriter().generateProxy(NoSuspend::class) }
    }
}

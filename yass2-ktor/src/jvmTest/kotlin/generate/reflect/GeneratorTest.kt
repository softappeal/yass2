@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.assertFailsWithMessage
import ch.softappeal.yass2.core.InternalApi
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

private class BodyProperty {
    @Suppress("unused") var x: Int = 0
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
        assertFailsWithMessage<IllegalArgumentException>(
            "class ch.softappeal.yass2.generate.reflect.BodyProperty must not have body properties"
        ) { generateBinarySerializer(BodyProperty::class) }
        assertFailsWithMessage<IllegalStateException>(
            "class ch.softappeal.yass2.generate.reflect.NoPrimaryConstructor must hava a primary constructor"
        ) { generateBinarySerializer(NoPrimaryConstructor::class) }
        assertFailsWithMessage<IllegalStateException>(
            "primary constructor parameter x of class ch.softappeal.yass2.generate.reflect.ConstructorParameterIsNotProperty must be a property"
        ) { generateBinarySerializer(ConstructorParameterIsNotProperty::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "class ch.softappeal.yass2.generate.reflect.NotRegularClass must be concrete"
        ) { generateBinarySerializer(NotRegularClass::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "class ch.softappeal.yass2.generate.reflect.AbstractClass must be concrete"
        ) { generateBinarySerializer(AbstractClass::class) }
        assertFailsWithMessage<IllegalStateException>(
            "enum class ch.softappeal.yass2.generate.reflect.Enum belongs to ConcreteAndEnumClasses"
        ) { codeWriter().generateBinarySerializer(listOf(MyEnumEncoder::class), listOf()) }
        assertFailsWithMessage<IllegalArgumentException>(
            "classes [kotlin.Int] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(IntBinaryEncoder::class), listOf(Int::class)) }
        assertFailsWithMessage<IllegalArgumentException>(
            "classes [ch.softappeal.yass2.generate.reflect.Enum] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(), listOf(Enum::class, Enum::class)) }
    }

    @Test
    fun proxy() {
        assertFailsWithMessage<IllegalArgumentException>(
            "ch.softappeal.yass2.generate.reflect.NotAnInterface must be an interface"
        ) { codeWriter().generateProxy(NotAnInterface::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "interface ch.softappeal.yass2.generate.reflect.Overloaded has overloaded methods [f]"
        ) { codeWriter().generateProxy(Overloaded::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "method ch.softappeal.yass2.generate.reflect.NoSuspend.noSuspend must be suspend"
        ) { codeWriter().generateProxy(NoSuspend::class) }
    }
}

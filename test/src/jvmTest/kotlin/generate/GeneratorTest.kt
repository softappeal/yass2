package ch.softappeal.yass2.generate

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import kotlin.IllegalArgumentException
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.Suppress
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

private class ConstructorParameterIsNotProperty(@Suppress("UNUSED_PARAMETER") x: Int)

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
            "body property x of ch.softappeal.yass2.generate.BodyPropertyNotVar must be var"
        ) { generateBinarySerializer(BodyPropertyNotVar::class) }
        assertFailsMessage<IllegalStateException>(
            "class ch.softappeal.yass2.generate.NoPrimaryConstructor must hava a primary constructor"
        ) { generateBinarySerializer(NoPrimaryConstructor::class) }
        assertFailsMessage<IllegalArgumentException>(
            "primary constructor parameter x of class ch.softappeal.yass2.generate.ConstructorParameterIsNotProperty must be a property"
        ) { generateBinarySerializer(ConstructorParameterIsNotProperty::class) }
        assertFailsMessage<IllegalArgumentException>(
            "class ch.softappeal.yass2.generate.NotRegularClass must be concrete"
        ) { generateBinarySerializer(NotRegularClass::class) }
        assertFailsMessage<IllegalArgumentException>(
            "class ch.softappeal.yass2.generate.AbstractClass must be concrete"
        ) { generateBinarySerializer(AbstractClass::class) }
        assertFailsMessage<IllegalStateException>(
            "enum class ch.softappeal.yass2.generate.Enum belongs to enumClasses"
        ) { codeWriter().generateBinarySerializer(listOf(MyEnumEncoder::class), listOf()) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [kotlin.Int] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(IntBinaryEncoder::class), listOf(Int::class)) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [ch.softappeal.yass2.generate.Enum] are duplicated"
        ) {
            codeWriter().generateBinarySerializer(listOf(), listOf(Enum::class, Enum::class))
        }
    }

    @Test
    fun proxy() {
        assertFailsMessage<IllegalArgumentException>(
            "ch.softappeal.yass2.generate.NotAnInterface must be an interface"
        ) { codeWriter().generateProxy(NotAnInterface::class) }
        assertFailsMessage<IllegalArgumentException>(
            "interface ch.softappeal.yass2.generate.Overloaded has overloaded methods [f]"
        ) { codeWriter().generateProxy(Overloaded::class) }
        assertFailsMessage<IllegalArgumentException>(
            "method ch.softappeal.yass2.generate.NoSuspend.noSuspend must be suspend"
        ) { codeWriter().generateProxy(NoSuspend::class) }
    }
}

@file:OptIn(TestingYassApi::class)

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.core.TestingYassApi
import ch.softappeal.yass2.core.assertFailsWithMessage
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

private fun map(): Map<Exception, RuntimeException>? = null
private const val MapType =
    "kotlin.collections.Map<kotlin.Exception /* = java.lang.Exception */, kotlin.RuntimeException /* = java.lang.RuntimeException */>?"

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
    fun removeComment() {
        assertEquals("", "/**/".removeComment())
        assertEquals("", " /**/".removeComment())
        assertEquals("", "  /**/".removeComment())
        assertEquals("12", "1 /*abc/**/2".removeComment())
        assertEquals("kotlin.Exception", "kotlin.Exception /* = java.lang.Exception */".removeComment())
        assertEquals(MapType, ::map.returnType.toString())
        assertEquals("kotlin.collections.Map<kotlin.Exception, kotlin.RuntimeException>?", MapType.removeComment())
        assertEquals(" 12 ", " 1  /*abc/* a b c * / */2 ".removeComment())
    }

    @Test
    fun binarySerializer() {
        fun generateBinarySerializer(klass: KClass<*>) {
            codeWriter().generateBinarySerializer(listOf(), listOf(klass))
        }
        assertFailsWithMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.BodyProperty' must not have body properties"
        ) { generateBinarySerializer(BodyProperty::class) }
        assertFailsWithMessage<IllegalStateException>(
            "class 'ch.softappeal.yass2.generate.NoPrimaryConstructor' must have a primary constructor"
        ) { generateBinarySerializer(NoPrimaryConstructor::class) }
        assertFailsWithMessage<IllegalStateException>(
            "primary constructor parameter 'x' of class 'ch.softappeal.yass2.generate.ConstructorParameterIsNotProperty' must be a property"
        ) { generateBinarySerializer(ConstructorParameterIsNotProperty::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.NotRegularClass' must be concrete"
        ) { generateBinarySerializer(NotRegularClass::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.AbstractClass' must be concrete"
        ) { generateBinarySerializer(AbstractClass::class) }
        assertFailsWithMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.Enum' belongs to 'concreteAndEnumClasses'"
        ) { codeWriter().generateBinarySerializer(listOf(MyEnumEncoder::class), listOf()) }
        assertFailsWithMessage<IllegalArgumentException>(
            "classes [kotlin.Int] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(IntBinaryEncoder::class), listOf(Int::class)) }
        assertFailsWithMessage<IllegalArgumentException>(
            "classes [ch.softappeal.yass2.generate.Enum] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(), listOf(Enum::class, Enum::class)) }
    }

    @Test
    fun proxy() {
        assertFailsWithMessage<IllegalArgumentException>(
            "'ch.softappeal.yass2.generate.NotAnInterface' must be an interface"
        ) { codeWriter().generateProxy(NotAnInterface::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "interface 'ch.softappeal.yass2.generate.Overloaded' has overloaded methods [f]"
        ) { codeWriter().generateProxy(Overloaded::class) }
        assertFailsWithMessage<IllegalArgumentException>(
            "method 'ch.softappeal.yass2.generate.NoSuspend.noSuspend' must be suspend"
        ) { codeWriter().generateProxy(NoSuspend::class) }
    }
}

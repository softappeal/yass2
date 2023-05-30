// NOTE: uncomment the following lines for testing
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [Simple::class], graphConcreteClasses = [Simple::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [Int::class])

// NOTE: uncomment the following lines for testing
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [EnumClass::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [AbstractClass::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NoPrimaryConstructor::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [ConstructorParameterIsNotProperty::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [BodyPropertyNotVar::class])

// NOTE: uncomment following line for testing
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [])

@file:Suppress("unused")

package ch.softappeal.yass2

import ch.softappeal.yass2.serialize.binary.*
import kotlin.test.*

// NOTE: uncomment the following line for testing
//@GenerateProxy
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

// NOTE: uncomment the following line for testing
//@GenerateProxy
private class NotAnInterface

private enum class EnumClass

private abstract class AbstractClass

private class ConstructorParameterIsNotProperty(x: Int) {
    init {
        println(x)
    }
}

@Suppress("ConvertSecondaryConstructorToPrimary")
private class NoPrimaryConstructor {
    constructor()
}

private class Simple

private class BodyPropertyNotVar {
    val x: Int = 0
}

class KspTest {
    @Test
    fun neededForImport() {
        println(IntEncoder::class)
    }
}

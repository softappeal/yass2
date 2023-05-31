// NOTE: uncomment following line for testing duplicated annotation
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [])

// NOTE: uncomment the following lines for testing
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [Simple::class], graphConcreteClasses = [Simple::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [Int::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [Gender::class, Gender::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [], graphConcreteClasses = [Gender::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [GenderEncoder::class], treeConcreteClasses = [])

// NOTE: uncomment the following lines for testing
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NotRegularClass::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [AbstractClass::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NoPrimaryConstructor::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [ConstructorParameterIsNotProperty::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [BodyPropertyNotVar::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [ImplicitGenericsNotAllowed::class])

@file:Suppress("unused")

package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.serialize.binary.*

// NOTE: uncomment the following line for testing
//@GenerateProxy
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

// NOTE: uncomment the following line for testing
//@GenerateProxy
private class NotAnInterface

private interface NotRegularClass

private class GenderEncoder : EnumEncoder<Gender>(Gender::class, enumValues())

private abstract class AbstractClass

private class ConstructorParameterIsNotProperty(@Suppress("UNUSED_PARAMETER") x: Int)

@Suppress("ConvertSecondaryConstructorToPrimary")
private class NoPrimaryConstructor {
    constructor()
}

private class Simple

private class BodyPropertyNotVar {
    val x: Int = 0
}

class ImplicitGenericsNotAllowed {
    var x = emptyList<Int>()
}

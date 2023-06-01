// /*1*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [])

// /*2*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [Simple::class], graphConcreteClasses = [Simple::class])
// /*3*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [Int::class])
// /*4*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [MyEnum::class, MyEnum::class])
// /*5*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [], graphConcreteClasses = [MyEnum::class])
// /*6*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [MyEnumEncoder::class], treeConcreteClasses = [])

// /*7*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NotRegularClass::class])
// /*8*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [AbstractClass::class])
// /*9*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NoPrimaryConstructor::class])
// /*10*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [ConstructorParameterIsNotProperty::class])
// /*11*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [BodyPropertyNotVar::class])
// /*12*/ @file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [ImplicitGenericsNotAllowed::class])

@file:Suppress("unused")

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import ch.softappeal.yass2.serialize.binary.*
import kotlin.test.*

// /*13*/ @GenerateProxy
private interface Overloaded {
    suspend fun f()
    suspend fun f(i: Int)
}

// /*14*/ @GenerateProxy
private class NotAnInterface

private interface NotRegularClass

private enum class MyEnum
private class MyEnumEncoder : EnumEncoder<MyEnum>(MyEnum::class, enumValues())

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

class ManualKspTest {
    @Test
    fun neededForImport() {
        println(GenerateProxy::class)
    }
}

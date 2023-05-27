// TODO: comment out the following lines for testing
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [Simple::class], graphConcreteClasses = [Simple::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [Int::class])

// TODO: comment out the following lines for testing
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [Color::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [AbstractClass::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NoPrimaryConstructor::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [ConstructorParameterIsNotProperty::class])
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [IntEncoder::class], treeConcreteClasses = [BodyPropertyNotVar::class])

// TODO: comment out the following line for testing duplicated annotation
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [])

package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.*
import kotlin.reflect.full.*
import kotlin.test.*

@Suppress("unused")
abstract class AbstractClass

@Suppress("unused")
class ConstructorParameterIsNotProperty(x: Int) {
    init {
        println(x)
    }
}

@Suppress("unused", "ConvertSecondaryConstructorToPrimary")
class NoPrimaryConstructor {
    constructor()
}

@Suppress("unused")
class Simple

@Suppress("unused")
class BodyPropertyNotVar {
    val x: Int = 0
}

class GenerateBinarySerializerTest {
    @Test
    fun neededForImport() {
        println(GenerateProxy::class)
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
}

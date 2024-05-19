package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.Calculator
import ch.softappeal.yass2.contract.child.NoSuspend
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.IntEncoder
import kotlin.io.path.Path
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

private interface MissingAnnotation

enum class Enum

class MyEnumEncoder : EnumEncoder<Enum>(Enum::class, enumValues())

private class NotEnum

class ReflectTest {
    @Test
    fun binarySerializer() {
        fun generateBinarySerializer(klass: KClass<*>) {
            StringBuilder().generateBinarySerializer(listOf(), listOf(), listOf(klass), listOf())
        }
        assertFailsMessage<IllegalArgumentException>(
            "body property 'x' of 'ch.softappeal.yass2.generate.reflect.BodyPropertyNotVar' must be 'var'"
        ) { generateBinarySerializer(BodyPropertyNotVar::class) }
        assertFailsMessage<IllegalStateException>(
            "class 'ch.softappeal.yass2.generate.reflect.NoPrimaryConstructor' must hava a primary constructor"
        ) { generateBinarySerializer(NoPrimaryConstructor::class) }
        assertFailsMessage<IllegalArgumentException>(
            "primary constructor parameter 'x' of class 'ch.softappeal.yass2.generate.reflect.ConstructorParameterIsNotProperty' must be a property"
        ) { generateBinarySerializer(ConstructorParameterIsNotProperty::class) }
        assertFailsMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.reflect.NotRegularClass' must be concrete"
        ) { generateBinarySerializer(NotRegularClass::class) }

        assertFailsMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.reflect.AbstractClass' must be concrete"
        ) { generateBinarySerializer(AbstractClass::class) }
        fun generateBinarySerializer(
            baseEncoderClasses: List<KClass<*>>, enumClasses: List<KClass<*>>,
            treeConcreteClasses: List<KClass<*>>, graphConcreteClasses: List<KClass<*>>,
        ) {
            generateBinarySerializer(
                baseEncoderClasses, enumClasses, treeConcreteClasses, graphConcreteClasses,
                false, Path("."), Mode.Verify, ""
            )
        }
        assertFailsMessage<IllegalArgumentException>(
            "class must not be duplicated"
        ) { generateBinarySerializer(listOf(), listOf(), listOf(Int::class), listOf(Int::class)) }
        assertFailsMessage<IllegalArgumentException>(
            "class must not be duplicated"
        ) { generateBinarySerializer(listOf(IntEncoder::class), listOf(), listOf(Int::class), listOf()) }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.reflect.Enum' belongs to 'enumClasses'"
        ) { generateBinarySerializer(listOf(), listOf(), listOf(Enum::class), listOf()) }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.reflect.Enum' belongs to 'enumClasses'"
        ) { generateBinarySerializer(listOf(), listOf(), listOf(), listOf(Enum::class)) }
        assertFailsMessage<IllegalArgumentException>(
            "enum classes must not be duplicated"
        ) { generateBinarySerializer(listOf(), listOf(Enum::class, Enum::class), listOf(), listOf()) }
        assertFailsMessage<IllegalArgumentException>(
            "class 'ch.softappeal.yass2.generate.reflect.NotEnum' in enumClasses must be enum"
        ) { generateBinarySerializer(listOf(), listOf(NotEnum::class), listOf(), listOf()) }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.reflect.Enum' belongs to 'enumClasses'"
        ) { generateBinarySerializer(listOf(MyEnumEncoder::class), listOf(), listOf(), listOf()) }
    }

    @Test
    fun dumper() {
        assertFailsMessage<IllegalArgumentException>(
            "class must not be duplicated"
        ) { generateDumper(listOf(Int::class), listOf(Int::class), Path("."), Mode.Verify, "") }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.reflect.Enum' must not be specified"
        ) { generateDumper(listOf(Enum::class), listOf(), Path("."), Mode.Verify, "") }
        assertFailsMessage<IllegalStateException>(
            "enum class 'ch.softappeal.yass2.generate.reflect.Enum' must not be specified"
        ) { generateDumper(listOf(), listOf(Enum::class), Path("."), Mode.Verify, "") }
    }

    @Test
    fun proxy() {
        assertFailsMessage<IllegalArgumentException>(
            "'ch.softappeal.yass2.generate.reflect.NotAnInterface' must be an interface"
        ) { StringBuilder().generateProxy(NotAnInterface::class) }
        assertFailsMessage<IllegalArgumentException>(
            "interface 'ch.softappeal.yass2.generate.reflect.Overloaded' must not overload functions"
        ) { StringBuilder().generateProxy(Overloaded::class) }
        assertFailsMessage<IllegalArgumentException>(
            "services [class ch.softappeal.yass2.contract.Calculator, class ch.softappeal.yass2.contract.child.NoSuspend] must be in same package"
        ) { generateProxy(setOf(Calculator::class, NoSuspend::class), Path("."), Mode.Verify) }
        assertFailsMessage<IllegalArgumentException>(
            "'class ch.softappeal.yass2.generate.reflect.MissingAnnotation' must be annotated with 'class ch.softappeal.yass2.GenerateProxy'"
        ) { generateProxy(setOf(MissingAnnotation::class), Path("."), Mode.Verify) }
    }
}

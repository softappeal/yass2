package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.InternalApi
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class, InternalApi::class)
private fun executeTest(message: String, source: String) {
    val result = KotlinCompilation().apply {
        classpaths = listOf(File("../yass2-core/build/classes/kotlin/jvm/main"))
        sources = listOf(SourceFile.kotlin("Source.kt", source))
        symbolProcessorProviders = listOf(Yass2Provider())
    }.compile()
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    assertTrue(result.messages.contains("Exception: $message"))
}

class GeneratorTest {
    @Test
    fun atMostOneAnnotation() {
        executeTest(
            "there can be at most one annotation 'ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses' in package 'test'",
            """
                package test
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses()
                class Generate1
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses()
                class Generate2
            """,
        )
    }

    @Ignore // TODO: review
    @Test
    fun binarySerializer() {

        /*

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
            "enum class ch.softappeal.yass2.generate.reflect.Enum belongs to concreteAndEnumClasses"
        ) { codeWriter().generateBinarySerializer(listOf(MyEnumEncoder::class), listOf()) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [kotlin.Int] are duplicated"
        ) { codeWriter().generateBinarySerializer(listOf(IntBinaryEncoder::class), listOf(Int::class)) }
        assertFailsMessage<IllegalArgumentException>(
            "classes [ch.softappeal.yass2.generate.reflect.Enum] are duplicated"
        ) {
            codeWriter().generateBinarySerializer(listOf(), listOf(Enum::class, Enum::class))
        }

        */

        executeTest(
            "body property x of test.BodyPropertyNotVar must be var",
            """
                package test
                class BodyPropertyNotVar {
                    val x: Int = 0
                }
                @ch.softappeal.yass2.serialize.GenerateSerializer([BodyPropertyNotVar::class], [ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "class test.NoPrimaryConstructor must hava a primary constructor",
            """
                package test
                class NoPrimaryConstructor {
                    constructor()
                }
                @ch.softappeal.yass2.serialize.GenerateSerializer([NoPrimaryConstructor::class], [ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "primary constructor parameter x of class test.ConstructorParameterIsNotProperty must be a property",
            """
                package test
                class ConstructorParameterIsNotProperty(x: Int)
                @ch.softappeal.yass2.serialize.GenerateSerializer([ConstructorParameterIsNotProperty::class], [ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "class test.NotRegularClass must be concrete",
            """
                package test
                interface NotRegularClass
                @ch.softappeal.yass2.serialize.GenerateSerializer([NotRegularClass::class], [ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "class test.AbstractClass must be concrete",
            """
                package test
                abstract class AbstractClass
                @ch.softappeal.yass2.serialize.GenerateSerializer([AbstractClass::class], [ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "enum class test.MyEnum belongs to enumClasses",
            """
                package test
                enum class MyEnum
                class MyEnumEncoder : ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder<MyEnum>(MyEnum::class, enumValues())
                @ch.softappeal.yass2.serialize.GenerateSerializer([], [MyEnumEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "classes [Int] are duplicated",
            """
                @ch.softappeal.yass2.serialize.GenerateSerializer([Int::class], [ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "classes [MyEnum] are duplicated",
            """
                enum class MyEnum
                @ch.softappeal.yass2.serialize.GenerateSerializer([MyEnum::class, MyEnum::class], [ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class], [])
                val x = 0
            """,
        )
        executeTest(
            "there can be at most one annotation GenerateSerializer in package test",
            """
                package test
                @ch.softappeal.yass2.serialize.GenerateSerializer([], [], [])
                val x1 = 0
                @ch.softappeal.yass2.serialize.GenerateSerializer([], [], [])
                val x2 = 0
            """,
        )
    }

    @Test
    fun proxy() {
        executeTest(
            "test.NotAnInterface must be an interface",
            """
                package test
                @ch.softappeal.yass2.core.Proxy
                class NotAnInterface
            """,
        )
        executeTest(
            "interface test.Overloaded has overloaded methods [f]",
            """
                package test
                @ch.softappeal.yass2.core.Proxy
                interface Overloaded {
                    suspend fun f()
                    suspend fun f(i: Int)
                }
            """,
        )
        executeTest(
            "method test.NoSuspend.noSuspend must be suspend",
            """
                package test
                @ch.softappeal.yass2.core.Proxy
                interface NoSuspend {
                    fun noSuspend f()
                }
            """,
        )
    }
}

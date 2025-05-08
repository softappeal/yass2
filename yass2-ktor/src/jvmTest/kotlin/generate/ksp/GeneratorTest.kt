package ch.softappeal.yass2.generate.ksp // TODO: review

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

    @Ignore
    @Test
    fun binarySerializer() {
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

    @Ignore
    @Test
    fun proxy() {
        executeTest(
            "test.NotAnInterface must be an interface",
            """
                package test
                @ch.softappeal.yass2.GenerateProxy
                class NotAnInterface
            """,
        )
        executeTest(
            "interface test.Overloaded has overloaded methods [f]",
            """
                package test
                @ch.softappeal.yass2.GenerateProxy
                interface Overloaded {
                    suspend fun f()
                    suspend fun f(i: Int)
                }
            """,
        )
    }
}

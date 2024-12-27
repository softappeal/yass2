package ch.softappeal.yass2.generate.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
private fun executeTest(message: String, source: String, lineNumber: Int) {
    val result = KotlinCompilation().apply {
        classpaths = listOf(File("../yass2-core/build/classes/kotlin/jvm/main"))
        sources = listOf(SourceFile.kotlin("Source.kt", source))
        symbolProcessorProviders = listOf(Yass2Provider())
    }.compile()
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    assertTrue(result.messages.contains("Exception: $message"))
    assertTrue(result.messages.contains(" @FileLocation(filePath="))
    assertTrue(result.messages.contains("/Source.kt, lineNumber=$lineNumber)"))
}

class GeneratorTest {
    @Test
    fun binarySerializer() {
        executeTest(
            "body property x of test.BodyPropertyNotVar must be var",
            """
                package test
                class BodyPropertyNotVar {
                    val x: Int = 0
                }
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [BodyPropertyNotVar::class])
                val x = 0
            """,
            6,
        )
        executeTest(
            "class test.NoPrimaryConstructor must hava a primary constructor",
            """
                package test
                class NoPrimaryConstructor {
                    constructor()
                }
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [NoPrimaryConstructor::class])
                val x = 0
            """,
            6,
        )
        executeTest(
            "primary constructor parameter x of class test.ConstructorParameterIsNotProperty must be a property",
            """
                package test
                class ConstructorParameterIsNotProperty(x: Int)
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [ConstructorParameterIsNotProperty::class])
                val x = 0
            """,
            4,
        )
        executeTest(
            "class test.NotRegularClass must be concrete",
            """
                package test
                interface NotRegularClass
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [NotRegularClass::class])
                val x = 0
            """,
            4,
        )
        executeTest(
            "class test.AbstractClass must be concrete",
            """
                package test
                abstract class AbstractClass
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [AbstractClass::class])
                val x = 0
            """,
            4,
        )
        executeTest(
            "enum class test.MyEnum belongs to enumClasses",
            """
                package test
                enum class MyEnum
                class MyEnumEncoder : ch.softappeal.yass2.serialize.binary.EnumEncoder<MyEnum>(MyEnum::class, enumValues())
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([MyEnumEncoder::class], [])
                val x = 0
            """,
            5,
        )
        executeTest(
            "classes [Int] are duplicated",
            """
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([ch.softappeal.yass2.serialize.binary.IntEncoder::class], [Int::class])
                val x = 0
            """,
            2,
        )
        executeTest(
            "classes [MyEnum] are duplicated",
            """
                enum class MyEnum
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [MyEnum::class, MyEnum::class])
                val x = 0
            """,
            3,
        )
        executeTest(
            "there can be at most one annotation GenerateBinarySerializer in package test",
            """
                package test
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [])
                val x1 = 0
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [])
                val x2 = 0
            """,
            2,
        )
    }

    @Test
    fun proxy() {
        executeTest(
            "test.NotAnInterface must be an interface",
            """
                package test
                @ch.softappeal.yass2.GenerateProxy
                class NotAnInterface
            """,
            3,
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
            3,
        )
    }
}

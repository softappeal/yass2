package ch.softappeal.yass2.generate.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
private fun executeTest(lineNumber: Int, message: String, source1: String, source2: String? = null) {
    val file1 = SourceFile.kotlin("Source1.kt", source1)
    val result = KotlinCompilation().apply {
        classpaths = listOf(File("../yass2-core/build/classes/kotlin/jvm/main"))
        sources = if (source2 == null) listOf(file1) else listOf(file1, SourceFile.kotlin("Source2.kt", source2))
        symbolProcessorProviders = listOf(Yass2Provider())
        kspArgs["yass2.enableLogging"] = "true"
    }.compile()
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    assertTrue(result.messages.contains(
        Regex("Exception: $message @FileLocation\\(filePath=.*/${if (source2 == null) "Source1" else "Source2"}.kt, lineNumber=$lineNumber\\)")
    ))
}

@Ignore
class GeneratorTest { // TODO: review
    @Test
    fun binarySerializer() {
        executeTest(
            2,
            "annotation 'ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer' must not be duplicated in package 'test'",
            """
                package test
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [], [], false)
                val x1 = 0
            """,
            """
                package test
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [], [], false)
                val x2 = 0
            """,
        )
        executeTest(
            1, "class must not be duplicated",
            """
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [Int::class], [Int::class], false)
                val x = 0
            """,
        )
        executeTest(
            1, "class must not be duplicated",
            """
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([ch.softappeal.yass2.serialize.binary.IntEncoder::class], [], [], [Int::class], false)
                val x = 0
            """,
        )
        executeTest(
            3, "class 'test.Test' in enumClasses must be enum",
            """
                package test
                class Test
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [Test::class], [], [], false)
                val x = 0
            """,
        )
        executeTest(
            2, "enum classes must not be duplicated",
            """
                enum class MyEnum
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [MyEnum::class, MyEnum::class], [], [], false)
                val x = 0
            """,
        )
        executeTest(
            3,
            "enum class 'test.MyEnum' belongs to 'enumClasses'",
            """
                package test
                enum class MyEnum
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [], [MyEnum::class], false)
                val x = 0
            """,
        )
        executeTest(
            3,
            "enum class 'test.MyEnum' belongs to 'enumClasses'",
            """
                package test
                enum class MyEnum
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [MyEnum::class], [], false)
                val x = 0
            """,
        )
        executeTest(
            4,
            "enum class 'test.MyEnum' belongs to 'enumClasses'",
            """
                package test
                enum class MyEnum
                class MyEnumEncoder : ch.softappeal.yass2.serialize.binary.EnumEncoder<MyEnum>(MyEnum::class, enumValues())
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([MyEnumEncoder::class], [], [], [], false)
                val x = 0
            """,
        )
        executeTest(
            2, "class 'test.NotRegularClass' must be concrete",
            """
                package test
                interface NotRegularClass
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [NotRegularClass::class], [], false)
                val x = 0
            """,
        )
        executeTest(
            2, "class 'test.AbstractClass' must be concrete",
            """
                package test
                abstract class AbstractClass
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [AbstractClass::class], [], false)
                val x = 0
            """,
        )
        executeTest(
            2, "class 'test.NoPrimaryConstructor' must hava a primary constructor",
            """
                package test
                class NoPrimaryConstructor {
                    constructor()
                }
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [NoPrimaryConstructor::class], [], false)
                val x = 0
            """,
        )
        executeTest(
            2, "primary constructor parameter 'x' of class 'test.ConstructorParameterIsNotProperty' must be a property",
            """
                package test
                class ConstructorParameterIsNotProperty(x: Int)
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [ConstructorParameterIsNotProperty::class], [], false)
                val x = 0
            """,
        )
        executeTest(
            3, "body property 'x' of 'test.BodyPropertyNotVar' must be 'var'",
            """
                package test
                class BodyPropertyNotVar {
                    val x: Int = 0
                }
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [BodyPropertyNotVar::class], [], false)
                val x = 0
            """,
        )
        executeTest(
            3, "generic type 'List<Int>' must not be implicit",
            """
                package test
                class ImplicitGenericsNotAllowed {
                    var x = emptyList<Int>()
                }
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [ImplicitGenericsNotAllowed::class], [], false)
                val x = 0
            """,
        )
    }

    @Test
    fun dumper() {
        executeTest(
            2, "annotation 'ch.softappeal.yass2.GenerateDumper' must not be duplicated in package 'test'",
            """
                package test
                @ch.softappeal.yass2.GenerateDumper([], [])
                val x1 = 0
            """,
            """
                package test
                @ch.softappeal.yass2.GenerateDumper([], [])
                val x2 = 0
            """,
        )
        executeTest(
            1, "class must not be duplicated",
            """
                @ch.softappeal.yass2.GenerateDumper([Int::class], [Int::class])
                val x = 0
            """,
        )
        executeTest(
            3, "enum class 'test.MyEnum' must not be specified",
            """
                package test
                enum class MyEnum
                @ch.softappeal.yass2.GenerateDumper([MyEnum::class], [])
                val x = 0
            """,
        )
        executeTest(
            3, "enum class 'test.MyEnum' must not be specified",
            """
                package test
                enum class MyEnum
                @ch.softappeal.yass2.GenerateDumper([], [MyEnum::class])
                val x = 0
            """,
        )
    }

    @Test
    fun mixed() {
        executeTest(
            2,
            "annotation 'ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer' and " +
                "'ch.softappeal.yass2.GenerateDumper' must not be duplicated in package 'test'",
            """
                package test
                @ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [], [], false)
                val x1 = 0
            """,
            """
                package test
                @ch.softappeal.yass2.GenerateDumper([], [])
                val x2 = 0
            """,
        )
    }

    @Test
    fun proxy() {
        executeTest(
            3, "interface 'test.Overloaded' must not overload functions",
            """
                package test
                @ch.softappeal.yass2.GenerateProxy
                interface Overloaded {
                    suspend fun f()
                    suspend fun f(i: Int)
                }
            """,
        )
        executeTest(
            3, "'test.NotAnInterface' must be an interface",
            """
                package test
                @ch.softappeal.yass2.GenerateProxy
                class NotAnInterface
            """,
        )
    }
}

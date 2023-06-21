package ch.softappeal.yass2.ksp

import com.tschuchort.compiletesting.*
import java.io.*
import kotlin.test.*

private fun executeTest(lineNumber: Int, message: String, source1: String, source2: String? = null) {
    val file1 = SourceFile.kotlin("Source1.kt", source1)
    val result = KotlinCompilation().apply {
        classpaths = listOf(File("../yass2-core/build/classes/kotlin/jvm/main"))
        sources = if (source2 == null) listOf(file1) else listOf(file1, SourceFile.kotlin("Source2.kt", source2))
        symbolProcessorProviders = listOf(Yass2Provider())
        kspArgs["yass2.enableLogging"] = "true"
    }.compile()
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    assertTrue(result.messages.contains(Regex("Exception: $message @FileLocation\\(filePath=.*/${if (source2 == null) "Source1" else "Source2"}.kt, lineNumber=$lineNumber\\)")))
}

class KspTest {
    @Test
    fun binarySerializer() {
        executeTest(
            1, "annotation 'ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer' must not be duplicated in package 'test'",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [], false)
                package test
            """,
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [], false)
                package test
            """,
        )
        executeTest(
            1, "class must not be duplicated",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [Int::class], [Int::class], false)
            """,
        )
        executeTest(
            1, "class must not be duplicated",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([ch.softappeal.yass2.serialize.binary.IntEncoder::class], [], [Int::class], false)
            """,
        )
        executeTest(
            1, "enum classes must not be duplicated",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [MyEnum::class, MyEnum::class], [], false)
                enum class MyEnum
            """,
        )
        executeTest(
            1, "enum class 'test.MyEnum' belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses'",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [MyEnum::class], false)
                package test
                enum class MyEnum
            """,
        )
        executeTest(
            1, "enum class 'test.MyEnum' belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses'",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([MyEnumEncoder::class], [], [], false)
                package test
                enum class MyEnum
                class MyEnumEncoder : ch.softappeal.yass2.serialize.binary.EnumEncoder<MyEnum>(MyEnum::class, enumValues())
            """,
        )
        executeTest(
            3, "'test.NotRegularClass' must be a regular class",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [NotRegularClass::class], [], false)
                package test
                interface NotRegularClass
            """,
        )
        executeTest(
            3, "class 'test.AbstractClass' must not be abstract",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [AbstractClass::class], [], false)
                package test
                abstract class AbstractClass
            """,
        )
        executeTest(
            3, "class 'test.NoPrimaryConstructor' must hava a primary constructor",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [NoPrimaryConstructor::class], [], false)
                package test
                class NoPrimaryConstructor {
                    constructor()
                }
            """,
        )
        executeTest(
            3, "primary constructor parameter 'x' of class 'test.ConstructorParameterIsNotProperty' must be a property",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [ConstructorParameterIsNotProperty::class], [], false)
                package test
                class ConstructorParameterIsNotProperty(x: Int)
            """,
        )
        executeTest(
            4, "body property 'x' of 'test.BodyPropertyNotVar' must be 'var'",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [BodyPropertyNotVar::class], [], false)
                package test
                class BodyPropertyNotVar {
                    val x: Int = 0
                }
            """,
        )
        executeTest(
            4, "generic type 'List<Int>' must not be implicit",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [ImplicitGenericsNotAllowed::class], [], false)
                package test
                class ImplicitGenericsNotAllowed {
                    var x = emptyList<Int>()
                }
            """,
        )
    }

    @Test
    fun dumper() {
        executeTest(
            1, "annotation 'ch.softappeal.yass2.GenerateDumper' must not be duplicated in package 'test'",
            """
                @file:ch.softappeal.yass2.GenerateDumper([], [])
                package test
            """,
            """
                @file:ch.softappeal.yass2.GenerateDumper([], [])
                package test
            """,
        )
        executeTest(
            1, "class must not be duplicated",
            """
                @file:ch.softappeal.yass2.GenerateDumper([Int::class], [Int::class])
            """,
        )
        executeTest(
            1, "enum class 'test.MyEnum' must not be specified",
            """
                @file:ch.softappeal.yass2.GenerateDumper([MyEnum::class], [])
                package test
                enum class MyEnum
            """,
        )
    }

    @Test
    fun mixed() {
        executeTest(
            1, "annotation 'ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer' and 'ch.softappeal.yass2.GenerateDumper' must not be duplicated in package 'test'",
            """
                @file:ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer([], [], [], false)
                package test
            """,
            """
                @file:ch.softappeal.yass2.GenerateDumper([], [])
                package test
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

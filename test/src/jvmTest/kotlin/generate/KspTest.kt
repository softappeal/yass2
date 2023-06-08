package ch.softappeal.yass2.generate

import com.tschuchort.compiletesting.*
import java.io.*
import kotlin.test.*

private fun executeTest(lineNumber: Int, message: String, source1: String, source2: String? = null) {
    val file1 = SourceFile.kotlin("Source1.kt", source1)
    val result = KotlinCompilation().apply {
        classpaths = listOf(File("../yass2-core/build/classes/kotlin/jvm/main"))
        sources = if (source2 == null) listOf(file1) else listOf(file1, SourceFile.kotlin("Source2.kt", source2))
        symbolProcessorProviders = listOf(YassProvider())
        kspArgs["enableLogging"] = "true"
    }.compile()
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    assertTrue(result.messages.contains(Regex("Exception: $message @FileLocation\\(filePath=.*/${if (source2 == null) "Source1" else "Source2"}.kt, lineNumber=$lineNumber\\)")))
}

class KspTest {
    @Test
    fun duplicatedAnnotation() {
        executeTest(
            1, "annotation 'ch.softappeal.yass2.GenerateBinarySerializerAndDumper' must not be duplicated in package 'test'",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [])
                package test
            """,
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [])
                package test
            """,
        )
    }

    @Test
    fun duplicatedEncoderType1() {
        executeTest(
            1, "encoder type must not be duplicated",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [Int::class], graphConcreteClasses = [Int::class])
            """,
        )
    }

    @Test
    fun duplicatedEncoderType2() {
        executeTest(
            1, "encoder type must not be duplicated",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [ch.softappeal.yass2.serialize.binary.IntEncoder::class], treeConcreteClasses = [], graphConcreteClasses = [Int::class])
            """,
        )
    }

    @Test
    fun duplicatedEnumClass() {
        executeTest(
            1, "enum classes must not be duplicated",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [MyEnum::class, MyEnum::class])
                enum class MyEnum
            """,
        )
    }

    @Test
    fun enumClassBelongsTo1() {
        executeTest(
            1, "enum class 'test.MyEnum' belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses'",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [], graphConcreteClasses = [MyEnum::class])
                package test
                enum class MyEnum
            """,
        )
    }

    @Test
    fun enumClassBelongsTo2() {
        executeTest(
            1, "enum class 'test.MyEnum' belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses'",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [MyEnumEncoder::class], treeConcreteClasses = [])
                package test
                enum class MyEnum
                class MyEnumEncoder : ch.softappeal.yass2.serialize.binary.EnumEncoder<MyEnum>(MyEnum::class, enumValues())
            """,
        )
    }

    @Test
    fun regularClass() {
        executeTest(
            3, "'test.NotRegularClass' must be a regular class",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NotRegularClass::class])
                package test
                interface NotRegularClass
            """,
        )
    }

    @Test
    fun abstractClass() {
        executeTest(
            3, "class 'test.AbstractClass' must not be abstract",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [AbstractClass::class])
                package test
                abstract class AbstractClass
            """,
        )
    }

    @Test
    fun noPrimaryConstructor() {
        executeTest(
            3, "class 'test.NoPrimaryConstructor' must hava a primary constructor",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [NoPrimaryConstructor::class])
                package test
                class NoPrimaryConstructor {
                    constructor()
                }
            """,
        )
    }

    @Test
    fun constructorParameterIsNotProperty() {
        executeTest(
            3, "primary constructor parameter 'x' of class 'test.ConstructorParameterIsNotProperty' must be a property",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [ConstructorParameterIsNotProperty::class])
                package test
                class ConstructorParameterIsNotProperty(x: Int)
            """,
        )
    }

    @Test
    fun bodyPropertyNotVar() {
        executeTest(
            4, "body property 'x' of 'test.BodyPropertyNotVar' must be 'var'",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [BodyPropertyNotVar::class])
                package test
                class BodyPropertyNotVar {
                    val x: Int = 0
                }
            """,
        )
    }

    @Test
    fun implicitGenericsNotAllowed() {
        executeTest(
            4, "generic type 'List<Int>' must not be implicit",
            """
                @file:ch.softappeal.yass2.GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [ImplicitGenericsNotAllowed::class])
                package test
                class ImplicitGenericsNotAllowed {
                    var x = emptyList<Int>()
                }
            """,
        )
    }

    @Test
    fun overloaded() {
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
    }

    @Test
    fun notAnInterface() {
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

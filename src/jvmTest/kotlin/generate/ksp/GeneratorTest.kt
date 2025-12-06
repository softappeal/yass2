@file:OptIn(ExperimentalCompilerApi::class)

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun compile(source: String, vararg classPaths: String) = KotlinCompilation().apply {
    useKsp2()
    classpaths = classPaths.map { File(it) }
    sources = listOf(SourceFile.kotlin("Source.kt", source))
    symbolProcessorProviders += Yass2Provider()
}.compile()

private fun executeTest(message: String, source: String) {
    val result = compile(source, "build/classes/kotlin/jvm/main")
    assertEquals(KotlinCompilation.ExitCode.INTERNAL_ERROR, result.exitCode)
    assertTrue(result.messages.contains("Exception: $message\n"))
}

class GeneratorTest {
    @Test
    fun binarySerializer() {
        executeTest(
            "class test.BodyProperty must not have body properties",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses(BodyProperty::class)
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects()
                class BodyProperty {
                    var x: Int = 0
                }
            """,
        )
        executeTest(
            "class test.NoPrimaryConstructor must hava a primary constructor",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses(NoPrimaryConstructor::class)
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects()
                class NoPrimaryConstructor {
                    constructor()
                }
            """,
        )
        executeTest(
            "primary constructor parameter x of class test.ConstructorParameterIsNotProperty must be a property",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses(ConstructorParameterIsNotProperty::class)
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects()
                class ConstructorParameterIsNotProperty(x: Int)
            """,
        )
        executeTest(
            "class test.NotRegularClass must be concrete",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses(NotRegularClass::class)
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects()
                interface NotRegularClass
            """,
        )
        executeTest(
            "class test.AbstractClass must be concrete",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses(AbstractClass::class)
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects()
                abstract class AbstractClass
            """,
        )
        executeTest(
            "enum class test.Enum belongs to ConcreteAndEnumClasses",
            """
                package test
                enum class Enum { One }
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses()
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects(MyEnumEncoder::class)
                object MyEnumEncoder : ch.softappeal.yass2.serialize.binary.BinaryEncoder<Enum>(Enum::class, {}, { Enum.One })
            """,
        )
        executeTest(
            "classes [kotlin.Int] are duplicated",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses(Int::class)
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects(ch.softappeal.yass2.serialize.binary.IntBinaryEncoder::class)
                class Generate
            """,
        )
        executeTest(
            "classes [test.Enum] are duplicated",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses(Enum::class, Enum::class)
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects()
                enum class Enum
            """,
        )
    }

    @Test
    fun proxy() {
        executeTest(
            "test.NotAnInterface must be an interface",
            """
                package test
                @ch.softappeal.yass2.Proxies(NotAnInterface::class)
                class NotAnInterface
            """,
        )
        executeTest(
            "interface test.Overloaded has overloaded methods [f]",
            """
                package test
                @ch.softappeal.yass2.Proxies(Overloaded::class)
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
                @ch.softappeal.yass2.Proxies(NoSuspend::class)
                interface NoSuspend {
                    fun noSuspend()
                }
            """,
        )
    }

    @Test
    fun annotations() {
        executeTest(
            "there can be at most one annotation ch.softappeal.yass2.serialize.ConcreteAndEnumClasses in package test",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses()
                class Generate1
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses()
                class Generate2
            """,
        )
        executeTest(
            "missing annotation ch.softappeal.yass2.serialize.ConcreteAndEnumClasses in package test",
            """
                package test
                @ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects()
                class Generate
            """,
        )
        executeTest(
            "missing annotations ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects or ch.softappeal.yass2.serialize.string.StringEncoderObjects in package test",
            """
                package test
                @ch.softappeal.yass2.serialize.ConcreteAndEnumClasses()
                class Generate
            """,
        )
    }

    @Test
    fun generate() {
        val result = compile(
            File("src/commonTest/kotlin/Contract.kt").readText(),
            "build/classes/kotlin/jvm/main",
            "build/classes/kotlin/jvm/test",
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertEquals(1, result.sourcesGeneratedBySymbolProcessor.count())
        val generated = result.sourcesGeneratedBySymbolProcessor.first()
        println(generated)
        assertEquals(File("src/commonTest/kotlin/$GENERATED_BY_YASS.kt").readText(), generated.readText())
    }
}

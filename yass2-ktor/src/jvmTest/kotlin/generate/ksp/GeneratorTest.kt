package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.InternalApi
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
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

    @Test
    fun binarySerializer() {
        executeTest(
            "body property x of test.BodyPropertyNotVar must be var",
            """
                package test
                class BodyPropertyNotVar {
                    val x: String = ""
                }
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses(BodyPropertyNotVar::class)
                @ch.softappeal.yass2.core.serialize.string.StringEncoderObjects()
                class Generate
            """,
        )
        executeTest(
            "class test.NoPrimaryConstructor must hava a primary constructor",
            """
                package test
                class NoPrimaryConstructor {
                    constructor()
                }
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses(NoPrimaryConstructor::class)
                @ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects()
                class Generate
            """,
        )
        executeTest(
            "primary constructor parameter x of class test.ConstructorParameterIsNotProperty must be a property",
            """
                package test
                class ConstructorParameterIsNotProperty(x: Int)
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses(ConstructorParameterIsNotProperty::class)
                @ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects()
                class Generate
            """,
        )
        executeTest(
            "class test.NotRegularClass must be concrete",
            """
                package test
                interface NotRegularClass
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses(NotRegularClass::class)
                @ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects()
                class Generate
            """,
        )
        executeTest(
            "class test.AbstractClass must be concrete",
            """
                package test
                abstract class AbstractClass
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses(AbstractClass::class)
                @ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects()
                class Generate
            """,
        )
        executeTest(
            "enum class test.Enum belongs to ConcreteAndEnumClasses",
            """
                package test
                enum class Enum { One }
                object MyEnumEncoder : ch.softappeal.yass2.core.serialize.binary.BinaryEncoder<Enum>(Enum::class, {}, { Enum.One })
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses()
                @ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects(MyEnumEncoder::class)
                class Generate
            """,
        )
        executeTest(
            "classes [kotlin.Int] are duplicated",
            """
                package test
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses(Int::class)
                @ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects(ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder::class)
                class Generate
            """,
        )
        executeTest(
            "classes [test.Enum] are duplicated",
            """
                package test
                enum class Enum
                @ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses(Enum::class, Enum::class)
                @ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects()
                class Generate
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

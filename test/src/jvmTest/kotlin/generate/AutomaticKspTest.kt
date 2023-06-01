package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import com.tschuchort.compiletesting.*
import java.io.*
import kotlin.io.path.*
import kotlin.test.*

private fun executeTest(testNumber: Int, file: String, lineNumber: Int, message: String) {
    fun source(file: String) = SourceFile.kotlin("$file.kt", Path("src/jvmTest/kotlin/generate/$file.kt").readAndFixLines().replace("// /*$testNumber*/ @", "@"))
    val result = KotlinCompilation().apply {
        classpaths = listOf(File("../yass2-core/build/classes/kotlin/jvm/main"))
        sources = listOf(source("ManualKspTest"), source("ManualKspTest2"))
        symbolProcessorProviders = listOf(YassProvider())
        kspArgs["enableLogging"] = "true"
    }.compile()
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    assertTrue(result.messages.contains(Regex("Exception: $message @FileLocation\\(filePath=.*/$file.kt, lineNumber=$lineNumber\\)")))
}

class AutomaticKspTest {
    @Test
    fun test() {
        executeTest(1, "ManualKspTest2", 1, "annotation 'ch.softappeal.yass2.GenerateBinarySerializerAndDumper' must not be duplicated in package 'ch.softappeal.yass2.generate'")
        executeTest(2, "ManualKspTest", 3, "encoder type must not be duplicated")
        executeTest(3, "ManualKspTest", 4, "encoder type must not be duplicated")
        executeTest(4, "ManualKspTest", 5, "enum classes must not be duplicated")
        executeTest(5, "ManualKspTest", 6, "enum class 'ch.softappeal.yass2.generate.MyEnum' belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses'")
        executeTest(6, "ManualKspTest", 7, "enum class 'ch.softappeal.yass2.generate.MyEnum' belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses'")
        executeTest(7, "ManualKspTest", 33, "'ch.softappeal.yass2.generate.NotRegularClass' must be a regular class")
        executeTest(8, "ManualKspTest", 38, "class 'ch.softappeal.yass2.generate.AbstractClass' must not be abstract")
        executeTest(9, "ManualKspTest", 43, "class 'ch.softappeal.yass2.generate.NoPrimaryConstructor' must hava a primary constructor")
        executeTest(10, "ManualKspTest", 40, "primary constructor parameter 'x' of class 'ch.softappeal.yass2.generate.ConstructorParameterIsNotProperty' must be a property")
        executeTest(11, "ManualKspTest", 50, "body property 'x' of 'ch.softappeal.yass2.generate.BodyPropertyNotVar' must be 'var'")
        executeTest(12, "ManualKspTest", 54, "generic type 'List<Int>' must not be implicit")
        executeTest(13, "ManualKspTest", 25, "interface 'ch.softappeal.yass2.generate.Overloaded' must not overload functions")
        executeTest(14, "ManualKspTest", 31, "'ch.softappeal.yass2.generate.NotAnInterface' must be an interface")
    }
}

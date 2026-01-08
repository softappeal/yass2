@file:OptIn(ExperimentalCompilerApi::class)

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.reflect.REFLECT_CODE
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private fun compile(generateMode: GenerateMode) = compile(
    File("src/commonTest/kotlin/Contract.kt").readText(),
    generateMode,
    "../yass2-core/build/classes/kotlin/jvm/main",
    "../yass2-coroutines/build/classes/kotlin/jvm/main",
    "build/classes/kotlin/jvm/test",
).apply {
    assertEquals(KotlinCompilation.ExitCode.OK, exitCode)
}

private val CODE = REFLECT_CODE.replace("package ch.softappeal.yass2.generate.reflect\n", "package ch.softappeal.yass2\n")

private fun readCode(platform: String) =
    File("build/generated/ksp/$platform/${platform}Test/kotlin/ch/softappeal/yass2/$GENERATED_BY_YASS.kt").readText()

private val JVM_CODE = readCode("jvm")

class GenerateTest {
    @Test
    fun actual() {
        assertEquals(CODE, JVM_CODE.replace("public actual fun ", "public fun "))
    }

    @Test
    fun inRepository() {
        val result = compile(GenerateMode.InRepository)
        assertEquals(0, result.sourcesGeneratedBySymbolProcessor.count())
        val generatedFile = result.outputDirectory.parentFile.resolve("sources/$GENERATED_BY_YASS.kt")
        assertEquals(CODE, generatedFile.readText())
    }

    @Test
    fun inBuildDir() {
        val result = compile(GenerateMode.InBuildDir)
        assertEquals(1, result.sourcesGeneratedBySymbolProcessor.count())
        val generatedFile = result.sourcesGeneratedBySymbolProcessor.first()
        assertEquals(CODE, generatedFile.readText())
    }

    @Test
    fun platforms() {
        File("build/generated/ksp").listFiles().forEach { file ->
            val platform = file.name
            if (platform == "metadata") return@forEach
            println("platform $platform")
            assertEquals(JVM_CODE, readCode(platform))
        }
    }
}

@file:OptIn(ExperimentalCompilerApi::class)

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.reflect.CODE
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private fun compile(generateMode: GenerateMode) = compile(
    File("test/Contract.kt").readText(),
    generateMode,
    // gradle
    "../yass2-core/build/classes/kotlin/jvm/main",
    "../yass2-coroutines/build/classes/kotlin/jvm/main",
    "build/classes/kotlin/jvm/test",
    // toolchain
    "../build/artifacts/CompiledJvmArtifact/yass2-corejvm/kotlin-output",
    "../build/artifacts/CompiledJvmArtifact/yass2-coroutinesjvm/kotlin-output",
    "../build/artifacts/CompiledJvmArtifact/testsjvmTest/kotlin-output",
).apply {
    assertEquals(KotlinCompilation.ExitCode.OK, exitCode)
}

class GenerateTest {
    @Test
    fun actual() {
        val gradle = File("build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/$GENERATED_BY_YASS.kt")
        val toolchain = File("../build/generated/tests/jvmTest/src/ksp/kotlin/ch/softappeal/yass2/$GENERATED_BY_YASS.kt")
        assertEquals(
            CODE,
            (if (gradle.exists()) gradle else toolchain)
                .readText()
                .replace("public actual fun ", "public fun "),
        )
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
}

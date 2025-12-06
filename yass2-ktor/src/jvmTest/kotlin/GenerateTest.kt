// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.reflect.GenerateMode
import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
import ch.softappeal.yass2.ksp.Generate
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

private fun CodeWriter.write() {
    generateProxies(Generate::class)
    generateBinarySerializer(Generate::class)
    generateStringEncoders(Generate::class)
}

private fun path(platform: String) = "build/generated/ksp/$platform/${platform}Test/kotlin/ch/softappeal/yass2/ksp"

class GenerateTest {
    @Test
    fun generateReflect() {
        generateFile("src/commonTest/kotlin", GenerateMode.Check, CodeWriter::write)
        generateFile("src/commonTest/kotlin", GenerateMode.Update, CodeWriter::write)
        generateFile("src/commonTest/kotlin", GenerateMode.Check, CodeWriter::write)
    }

    @Test
    fun generateKsp() {
        Generate.generateFile(path("jvm"), GenerateMode.Check, CodeWriter::write)
    }

    @Test
    fun generatePlatform() {
        fun read(platform: String) = Path(path(platform)).resolve("$GENERATED_BY_YASS.kt").readText()
        val jvm = read("jvm")
        File("build/generated/ksp").listFiles()?.forEach { file ->
            val platform = file.name
            if (platform == "metadata") return@forEach
            println("platform $platform")
            assertEquals(jvm, read(platform))
        }
    }
}

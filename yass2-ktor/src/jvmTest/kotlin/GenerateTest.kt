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
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

private fun CodeWriter.write() {
    generateProxies(Generate::class)
    generateBinarySerializer(Generate::class)
    generateStringEncoders(Generate::class)
}

class GenerateTest {
    @Test
    fun generateReflect() {
        generateFile("src/commonTest/kotlin", GenerateMode.Check, CodeWriter::write)
        generateFile("src/commonTest/kotlin", GenerateMode.Update, CodeWriter::write)
        generateFile("src/commonTest/kotlin", GenerateMode.Check, CodeWriter::write)
    }

    @Test
    fun generateKsp() {
        Generate
            .generateFile("build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/ksp", GenerateMode.Check, CodeWriter::write)
    }

    @Test
    fun generatePlatform() {
        fun read(path: String) =
            Path("build/generated/ksp/$path/kotlin/ch/softappeal/yass2/ksp").resolve("$GENERATED_BY_YASS.kt").readText()

        val jvm = read("jvm/jvmTest")

        fun assert(path: String) {
            val platform = try {
                read(path)
            } catch (_: Exception) {
                println("no platform $path")
                return
            }
            assertEquals(jvm, platform)
            println("platform $path ok")
        }
        // TODO: don't forget to add new platforms
        assert("js/jsTest")
        assert("wasmJs/wasmJsTest")
        assert("linuxArm64/linuxArm64Test")
        assert("linuxX64/linuxX64Test")
    }
}

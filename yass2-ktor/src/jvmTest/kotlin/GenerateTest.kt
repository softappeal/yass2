// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.reflect.GenerateMode
import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
import ch.softappeal.yass2.ksp.Generate
import kotlin.test.Test

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
}

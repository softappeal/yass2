// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.reflect.GenerateMode
import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateCode
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private fun CodeWriter.generate() {
    generateProxies(Generate::class)
    generateBinarySerializer(Generate::class)
    generateStringEncoders(Generate::class)
}

class GenerateTest {
    @Test
    fun generateCode() {
        assertEquals(
            File("src/commonTest/kotlin/$GENERATED_BY_YASS.kt").readText(),
            generateCode(CodeWriter::generate),
        )
    }

    @Test
    fun generateFile() {
        listOf(
            GenerateMode.Check,
            GenerateMode.Update,
            GenerateMode.Check,
        ).forEach { generateFile("src/commonTest/kotlin", it, CodeWriter::generate) }
    }
}

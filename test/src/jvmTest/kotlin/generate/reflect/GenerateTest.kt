// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Generate
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private const val REFLECT_DIR = "src/jvmTest/kotlin/generate/reflect"

val REFLECT_CODE = File("$REFLECT_DIR/$GENERATED_BY_YASS.kt").readText()

class GenerateTest {
    @Test
    fun generateCode() {
        assertEquals(REFLECT_CODE, generateCode(Generate::class))
    }

    @Test
    fun generateFile() {
        listOf(
            GenerateMode.Check,
            GenerateMode.Update,
            GenerateMode.Check,
        ).forEach { generateFile(REFLECT_DIR, it, Generate::class) }
    }
}

// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Generate
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import java.io.File
import kotlin.test.Test

private const val DIR = "src/jvmTest/kotlin/generate/reflect"

val CODE = File("$DIR/$GENERATED_BY_YASS.kt")
    .readText()
    .replace("package ch.softappeal.yass2.generate.reflect\n", "package ch.softappeal.yass2\n")

class GenerateTest {
    @Test
    fun generateFile() {
        generateFile(DIR, Generate::class)
    }
}

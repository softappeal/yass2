package ch.softappeal.yass2.generate

import ch.softappeal.yass2.generate.reflect.generateFile
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.test.Test

fun readGeneratedByYass() = File("test@jvm/$GENERATED_BY_YASS.kt").readText().fixLines()

class ReflectGenerateTest {
    @Test
    fun generate() {
        val buildDir = "../build"
        Path("$buildDir/$GENERATED_BY_YASS.kt").writeText(readGeneratedByYass())
        generateFile(buildDir, Generate::class)
    }
}

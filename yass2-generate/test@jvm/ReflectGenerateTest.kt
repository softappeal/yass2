package ch.softappeal.yass2.generate

import ch.softappeal.yass2.generate.reflect.generateFile
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.test.Test

const val GeneratedByYass = "test@jvm/$GENERATED_BY_YASS.kt"

class ReflectGenerateTest {
    @Test
    fun generate() {
        val buildDir = "../build"
        Path(GeneratedByYass).copyTo(Path("$buildDir/$GENERATED_BY_YASS.kt"), overwrite = true)
        generateFile(buildDir, Generate::class)
    }
}

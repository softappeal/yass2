// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.reflect.generateFile
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.test.Test

class ReflectGenerateTest {
    @Test
    fun generate() {
        val buildDir = "../build"
        Path("test/$GENERATED_BY_YASS.kt").copyTo(Path("$buildDir/$GENERATED_BY_YASS.kt"), overwrite = true)
        generateFile(buildDir, Generate::class)
    }
}

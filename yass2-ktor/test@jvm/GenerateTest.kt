package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.generate.reflect.generateFile
import kotlin.test.Test

class GenerateTest {
    @Test
    fun generate() {
        generateFile("test", Generate::class)
    }
}

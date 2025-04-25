package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxies
import ch.softappeal.yass2.generate.generateStringEncoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/commonMain/kotlin",
            "ch.softappeal.yass2.tutorial",
        ) {
            generateProxies(Services)
            generateStringEncoders(EncoderObjects, ConcreteClasses)
        }
    }
}

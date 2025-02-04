package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.GenerateMode
import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxies
import ch.softappeal.yass2.generate.generateUtf8Encoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/commonMain/kotlin/$GENERATED_BY_YASS",
            "ch.softappeal.yass2.tutorial",
            GenerateMode.Verify,
        ) {
            generateProxies(Services)
            generateUtf8Encoders(EncoderObjects, ConcreteClasses)
        }
    }
}

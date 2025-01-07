package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.generate.Mode
import ch.softappeal.yass2.generate.generate
import ch.softappeal.yass2.generate.generateProxies
import ch.softappeal.yass2.generate.generateTextSerializer
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonMain/kotlin",
            "ch.softappeal.yass2.tutorial",
            Mode.Verify,
        ) {
            generateProxies(Services)
            generateTextSerializer(EncoderClasses, ConcreteClasses)
        }
    }
}

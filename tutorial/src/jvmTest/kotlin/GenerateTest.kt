package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/commonMain/kotlin",
            "ch.softappeal.yass2.tutorial",
        ) {
            generateProxies(Services)
            generateStringEncoders(EncoderObjects, ConcreteAndEnumClasses)
        }
    }
}

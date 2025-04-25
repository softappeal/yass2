package ch.softappeal.yass2.contract

import ch.softappeal.yass2.generate.generateBinarySerializer
import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxies
import ch.softappeal.yass2.generate.generateStringEncoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/commonTest/kotlin/contract",
            "ch.softappeal.yass2.contract",
        ) {
            generateProxies(listOf(Calculator::class, Echo::class))
            generateBinarySerializer(BinaryEncoderObjects, ConcreteClasses)
            generateStringEncoders(StringEncoderObjects, ConcreteClasses)
        }
    }
}

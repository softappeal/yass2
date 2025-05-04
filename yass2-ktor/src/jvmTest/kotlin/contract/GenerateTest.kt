package ch.softappeal.yass2.contract

import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
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

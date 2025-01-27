package ch.softappeal.yass2.contract

import ch.softappeal.yass2.coroutines.FlowService
import ch.softappeal.yass2.generate.generate
import ch.softappeal.yass2.generate.generateBinarySerializer
import ch.softappeal.yass2.generate.generateProxy
import ch.softappeal.yass2.generate.generateUtf8Encoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonTest/kotlin/contract",
            "ch.softappeal.yass2.contract",
        ) {
            listOf(Calculator::class, Echo::class, Mixed::class, FlowService::class).forEach(::generateProxy)
            generateBinarySerializer(BinaryEncoderObjects, ConcreteClasses)
            generateUtf8Encoders(Utf8EncoderObjects, ConcreteClasses)
        }
    }
}

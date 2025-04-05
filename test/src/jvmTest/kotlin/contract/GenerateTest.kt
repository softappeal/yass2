package ch.softappeal.yass2.contract

import ch.softappeal.yass2.flow.FlowService
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.GenerateMode
import ch.softappeal.yass2.generate.generateBinarySerializer
import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxies
import ch.softappeal.yass2.generate.generateStringEncoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/commonTest/kotlin/contract/$GENERATED_BY_YASS",
            "ch.softappeal.yass2.contract",
            GenerateMode.Verify,
        ) {
            generateProxies(listOf(Calculator::class, Echo::class, FlowService::class))
            generateBinarySerializer(BinaryEncoderObjects, ConcreteClasses)
            generateStringEncoders(StringEncoderObjects, ConcreteClasses)
        }
    }
}

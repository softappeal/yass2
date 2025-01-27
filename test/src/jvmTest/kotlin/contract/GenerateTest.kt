package ch.softappeal.yass2.contract

import ch.softappeal.yass2.coroutines.FlowService
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.GenerateMode
import ch.softappeal.yass2.generate.generateBinarySerializer
import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxy
import ch.softappeal.yass2.generate.generateUtf8Encoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/commonTest/kotlin/contract/$GENERATED_BY_YASS",
            "ch.softappeal.yass2.contract",
            GenerateMode.Verify,
        ) {
            listOf(Calculator::class, Echo::class, Mixed::class, FlowService::class).forEach(::generateProxy)
            generateBinarySerializer(BinaryEncoderObjects, ConcreteClasses)
            generateUtf8Encoders(Utf8EncoderObjects, ConcreteClasses)
        }
    }
}

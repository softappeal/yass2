package ch.softappeal.yass2.generate

import ch.softappeal.yass2.BinaryEncoderObjects
import ch.softappeal.yass2.ConcreteAndEnumClasses
import ch.softappeal.yass2.Services
import ch.softappeal.yass2.StringEncoderObjects
import kotlin.test.Test

class GenerateTest {
    @Test
    fun generate() {
        generateFile(
            "src/commonTest/kotlin",
            "ch.softappeal.yass2",
        ) {
            generateProxies(Services)
            generateBinarySerializer(BinaryEncoderObjects, ConcreteAndEnumClasses)
            generateStringEncoders(StringEncoderObjects, ConcreteAndEnumClasses)
        }
    }
}

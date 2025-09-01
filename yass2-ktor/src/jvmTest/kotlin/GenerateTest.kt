package ch.softappeal.yass2

import ch.softappeal.yass2.generate.generateBinarySerializer
import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxies
import ch.softappeal.yass2.generate.generateStringEncoders
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

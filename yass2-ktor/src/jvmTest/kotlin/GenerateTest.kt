package ch.softappeal.yass2

import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
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

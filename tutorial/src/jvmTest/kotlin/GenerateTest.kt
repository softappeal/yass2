package tutorial

import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun generate() {
        generateFile(
            "src/commonMain/kotlin",
            "tutorial",
        ) {
            generateProxies(Services)
            generateStringEncoders(StringEncoderObjects, ConcreteAndEnumClasses)
        }
    }
}

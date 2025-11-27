package tutorial

import ch.softappeal.yass2.generate.GenerateMode
import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxies
import ch.softappeal.yass2.generate.generateStringEncoders
import kotlin.test.Test

class GenerateTest {
    @Test
    fun generate() {
        generateFile(
            "src/commonMain/kotlin",
            "tutorial",
            GenerateMode.Check, // or GenerateMode.Update
        ) {
            generateProxies(Services)
            generateStringEncoders(StringEncoderObjects, ConcreteAndEnumClasses)
        }
    }
}

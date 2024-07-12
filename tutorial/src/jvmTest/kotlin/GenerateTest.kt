package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.generate.Mode
import ch.softappeal.yass2.generate.generate
import ch.softappeal.yass2.generate.generateBinarySerializerAndDumper
import ch.softappeal.yass2.generate.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonMain/kotlin",
            "ch.softappeal.yass2.tutorial",
            Mode.Verify,
        ) {
            generateProxy(Services)
            generateBinarySerializerAndDumper(BaseEncoders, TreeConcreteClasses)
        }
    }
}

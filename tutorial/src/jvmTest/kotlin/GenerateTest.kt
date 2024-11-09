package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.generate.reflect.Mode
import ch.softappeal.yass2.generate.reflect.generate
import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonMain/kotlin",
            "ch.softappeal.yass2.tutorial",
            Mode.Verify,
        ) {
            generateProxy(Calculator::class)
            generateProxy(NewsListener::class)
            generateBinarySerializer(::ContractSerializer)
        }
    }
}

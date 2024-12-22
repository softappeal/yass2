package ch.softappeal.yass2.contract

import ch.softappeal.yass2.generate.reflect.Mode
import ch.softappeal.yass2.generate.reflect.generate
import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonTest/kotlin/contract/reflect",
            "ch.softappeal.yass2.contract.reflect",
            Mode.Verify,
        ) {
            listOf(Calculator::class, Echo::class, Mixed::class).forEach(::generateProxy)
            generateBinarySerializer(::ContractSerializer)
        }
    }
}

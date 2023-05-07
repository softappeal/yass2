package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.ksp.*
import kotlin.io.path.*
import kotlin.test.*

class GenerateTest {
    @Test
    fun test() {
        generateAll(
            Path("src/commonMain/kotlin"),
            "ch.softappeal.yass2.tutorial.contract",
            ServiceIds,
            ::BaseEncoders, ConcreteClasses,
        )
    }
}

package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.generate.GenerateMode
import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun generate() {
        generateFile("src/commonMain/kotlin/flow", GenerateMode.Check) {
            generateProxy(FlowService::class)
        }
    }
}

package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/commonMain/kotlin/flow",
            "ch.softappeal.yass2.coroutines.flow",
        ) {
            generateProxy(FlowService::class)
        }
    }
}

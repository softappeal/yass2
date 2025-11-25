package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun generate() {
        generateFile(
            "src/commonMain/kotlin/flow",
            "ch.softappeal.yass2.coroutines.flow",
        ) {
            generateProxy(FlowService::class)
        }
    }
}

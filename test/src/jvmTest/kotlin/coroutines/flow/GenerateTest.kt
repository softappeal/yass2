package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.generate.reflect.GenerateMode
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import kotlin.test.Test

class GenerateTest {
    @Test
    @Proxies(FlowService::class)
    fun generate() {
        generateFile("../yass2-coroutines/src/commonMain/kotlin/flow", GenerateMode.Check) {
            generateProxies(::generate)
        }
    }
}

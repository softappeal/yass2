package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.Proxies
import ch.softappeal.yass2.generate.reflect.GenerateMode
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxies
import kotlin.test.Test

class GenerateTest {
    @Test
    @Proxies(FlowService::class)
    fun generate() {
        generateFile("src/commonTest/kotlin/coroutines/flow", GenerateMode.Check) {
            generateProxies(::generate)
        }
    }
}

package ch.softappeal.yass2.remote.flow

import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.remote.Proxies
import kotlin.test.Test

@Proxies(FlowService::class)
class GenerateTest {
    @Test
    fun generate() {
        generateFile("src/flow", GenerateTest::class)
    }
}

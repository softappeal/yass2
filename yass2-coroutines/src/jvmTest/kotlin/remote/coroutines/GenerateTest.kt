package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.generate.Mode
import ch.softappeal.yass2.generate.generate
import ch.softappeal.yass2.generate.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonMain/kotlin/remote/coroutines",
            "ch.softappeal.yass2.remote.coroutines",
            Mode.Verify,
        ) {
            generateProxy(FlowService::class)
        }
    }
}

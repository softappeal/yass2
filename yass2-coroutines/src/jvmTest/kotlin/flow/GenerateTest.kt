package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.coroutines.session.Heartbeat
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun flow() {
        generateFile(
            "src/commonMain/kotlin/flow",
            "ch.softappeal.yass2.coroutines.flow",
        ) {
            generateProxy(FlowService::class)
        }
    }

    @Test
    fun heartbeat() {
        generateFile(
            "src/commonMain/kotlin/session",
            "ch.softappeal.yass2.coroutines.session",
        ) {
            generateProxy(Heartbeat::class)
        }
    }
}

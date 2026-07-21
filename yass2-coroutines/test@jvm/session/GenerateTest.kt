package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.generate.generateFile
import ch.softappeal.yass2.generate.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun generate() {
        generateFile("src/session") {
            generateProxy(KeepAlive::class)
        }
    }
}

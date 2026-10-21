package ch.softappeal.yass2.remote.session

import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.remote.Proxies
import kotlin.test.Test

@Proxies(KeepAlive::class)
class GenerateTest {
    @Test
    fun generate() {
        generateFile("src/session", GenerateTest::class)
    }
}

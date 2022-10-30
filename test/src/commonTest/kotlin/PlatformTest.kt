package ch.softappeal.yass2

import kotlin.test.*

class PlatformTest {
    @Test
    fun testGetPlatform() {
        println("Platform: ${getPlatform()}")
    }

    @Test
    fun testRunOnPlatforms1() {
        runOnPlatforms {
            fail()
        }
    }

    @Test
    fun testRunOnPlatforms2() {
        var run = false
        runOnPlatforms(Platform.Jvm, Platform.Js, Platform.Linux, Platform.Mac) {
            assertFalse(run)
            run = true
        }
        assertTrue(run)
    }
}

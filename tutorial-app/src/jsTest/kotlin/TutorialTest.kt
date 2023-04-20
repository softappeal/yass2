package ch.softappeal.yass2.tutorial

import kotlinx.coroutines.test.*
import kotlin.test.*

class TutorialTest {
    @Test
    fun test() = runTest {
        showUsage()
    }
}

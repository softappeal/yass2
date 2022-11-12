package ch.softappeal.yass2.tutorial

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class TutorialTestJs {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test() = runTest {
        showUsage()
    }
}
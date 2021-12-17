package ch.softappeal.yass2.tutorial

import kotlinx.coroutines.*
import kotlin.test.*

class TutorialTestJs {
    @Test
    fun test() = @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
        showUsage()
    }
}

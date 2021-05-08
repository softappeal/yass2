package ch.softappeal.yass2

import kotlin.test.*
import kotlin.time.*

inline fun performance(iterations: Int, action: () -> Unit) {
    println("iterations: $iterations")
    repeat(2) {
        val timeMs = @OptIn(ExperimentalTime::class) measureTime {
            repeat(iterations) { action() }
        }.inWholeMilliseconds
        println("  one sample: ${1_000_000 * timeMs / iterations}ns total time: ${timeMs}ms")
    }
}

inline fun <reified T : Throwable> assertPlatform(vararg expectedMessages: String, block: () -> Unit) {
    val actualMessage = assertFailsWith(T::class, block).message!!
    println(actualMessage)
    assertTrue(actualMessage in expectedMessages)
}

package ch.softappeal.yass2

import kotlin.test.*
import kotlin.time.*

@OptIn(ExperimentalTime::class)
inline fun performance(iterations: Int, action: () -> Unit) {
    println("iterations: $iterations")
    repeat(2) {
        val timeMs = measureTime {
            repeat(iterations) { action() }
        }.inMilliseconds
        println("  one sample: ${1_000_000 * timeMs / iterations}ns total time: ${timeMs}ms")
    }
}

inline fun <reified T : Throwable> assertPlatform(vararg expectedMessages: String, block: () -> Unit) {
    val actualMessage = assertFailsWith(T::class, block).message!!
    println(actualMessage)
    assertTrue(actualMessage in expectedMessages)
}

package ch.softappeal.yass2.core

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.measureTime

inline fun performance(iterations: Int, action: () -> Unit) {
    println("iterations: $iterations")
    repeat(2) {
        val timeMillis = measureTime {
            repeat(iterations) { action() }
        }.inWholeMilliseconds
        println("  one sample: ${1_000_000 * timeMillis / iterations}ns total time: ${timeMillis}ms")
    }
}

inline fun <reified E : Exception> assertFailsMessage(expectedMessage: String, block: () -> Unit) =
    assertEquals(expectedMessage, assertFailsWith(E::class, block).message)

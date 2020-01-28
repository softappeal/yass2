package ch.softappeal.yass2

import kotlin.test.*

inline fun performance(iterations: Int, action: () -> Unit) {
    println("iterations: $iterations")
    repeat(2) {
        val time = yassMeasureTimeMillis {
            repeat(iterations) { action() }
        }
        println("  one sample: ${1_000_000 * time / iterations}ns total time: ${time}ms")
    }
}

inline fun <reified T : Throwable> assertPlatform(vararg expectedMessages: String, block: () -> Unit) {
    val actualMessage = assertFailsWith(T::class, block).message!!
    println(actualMessage)
    assertTrue(actualMessage in expectedMessages)
}

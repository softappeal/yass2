package ch.softappeal.yass2

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail
import kotlin.time.measureTime

/** Needed because [assertFailsWith] doesn't work with js. */
suspend inline fun <reified T : Throwable> assertSuspendFailsWith(noinline block: suspend () -> Unit): T =
    assertSuspendFailsWith(T::class, block) as T

suspend fun assertSuspendFailsWith(exceptionClass: KClass<*>, block: suspend () -> Unit): Throwable {
    try {
        block()
    } catch (t: Throwable) {
        if (exceptionClass.isInstance(t)) return t
    }
    fail()
}

inline fun performance(iterations: Int, action: () -> Unit) {
    println("iterations: $iterations")
    repeat(2) {
        val timeMillis = measureTime {
            repeat(iterations) { action() }
        }.inWholeMilliseconds
        println("  one sample: ${1_000_000 * timeMillis / iterations}ns total time: ${timeMillis}ms")
    }
}

inline fun <reified T : Throwable> assertFailsMessage(expectedMessage: String, block: () -> Unit) =
    assertEquals(expectedMessage, assertFailsWith(T::class, block).message)

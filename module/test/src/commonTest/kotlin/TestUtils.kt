package ch.softappeal.yass2

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.reflect.*
import kotlin.test.*
import kotlin.time.*

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

fun yassRunBlocking(block: suspend CoroutineScope.() -> Unit) {
    @OptIn(ExperimentalCoroutinesApi::class)
    runTest(testBody = block)
}

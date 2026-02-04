@file:OptIn(ExperimentalAtomicApi::class)

package ch.softappeal.yass2.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

private data class MyException(val data: Int) : RuntimeException()

private val myException = MyException(123)

private suspend fun AtomicInt.delay(timeMillis: Long, value: Int) {
    incrementAndFetch()
    delay(timeMillis)
    assertEquals(value, load())
    incrementAndFetch()
}

class CoroutinesTest {
    @Test
    fun bothCompleted() = runTest {
        val counter = AtomicInt(0)
        val shortJob: Job
        val longJob: Job
        coroutineScope {
            shortJob = launch { counter.delay(2000, 4) }
            longJob = launch { counter.delay(3000, 5) }
            counter.delay(1000, 3)
        }
        assertTrue(shortJob.isCompleted)
        assertFalse(shortJob.isCancelled)
        assertTrue(longJob.isCompleted)
        assertFalse(longJob.isCancelled)
        assertEquals(6, counter.load())
    }

    @Test
    fun scopeException() = runTest {
        val counter = AtomicInt(0)
        var shortJob: Job? = null
        var longJob: Job? = null
        try {
            coroutineScope {
                shortJob = launch { counter.delay(2000, 999) }
                longJob = launch { counter.delay(3000, 999) }
                counter.delay(1000, 3)
                assertTrue(shortJob.isActive)
                assertTrue(longJob.isActive)
                throw myException
            }
        } catch (e: MyException) {
            assertSame(myException, e)
            assertTrue(shortJob!!.isCancelled)
            assertTrue(longJob!!.isCancelled)
            assertEquals(4, counter.load())
        }
    }

    @Test
    fun shortException() = runTest {
        val counter = AtomicInt(0)
        var shortJob: Job? = null
        var longJob: Job? = null
        try {
            coroutineScope {
                shortJob = launch { counter.delay(2000, 4); throw myException }
                longJob = launch { counter.delay(3000, 999) }
                counter.delay(1000, 3)
            }
            fail()
        } catch (e: MyException) {
            assertSame(myException, e)
            assertTrue(shortJob!!.isCancelled)
            assertTrue(longJob!!.isCancelled)
            assertEquals(5, counter.load())
        }
    }

    @Test
    fun longException() = runTest {
        val counter = AtomicInt(0)
        var shortJob: Job? = null
        var longJob: Job? = null
        try {
            coroutineScope {
                shortJob = launch { counter.delay(2000, 4) }
                longJob = launch { counter.delay(3000, 5); throw myException }
                counter.delay(1000, 3)
            }
            fail()
        } catch (e: MyException) {
            assertSame(myException, e)
            assertTrue(shortJob!!.isCompleted)
            assertFalse(shortJob.isCancelled)
            assertTrue(longJob!!.isCancelled)
            assertEquals(6, counter.load())
        }
    }

    @Test
    fun scopeCancel() = runTest {
        val counter = AtomicInt(0)
        var shortJob: Job? = null
        var longJob: Job? = null
        try {
            coroutineScope {
                shortJob = launch { counter.delay(2000, 999) }
                longJob = launch { counter.delay(3000, 999) }
                counter.delay(1000, 3)
                cancel()
            }
            fail()
        } catch (e: CancellationException) {
            println(e)
            assertTrue(shortJob!!.isCancelled)
            assertTrue(longJob!!.isCancelled)
            assertEquals(4, counter.load())
        }
    }

    @Test
    fun shortCancel() = runTest {
        val counter = AtomicInt(0)
        val shortJob: Job
        val longJob: Job
        coroutineScope {
            shortJob = launch { counter.delay(2000, 999) }
            longJob = launch { counter.delay(3000, 4) }
            counter.delay(1000, 3)
            shortJob.cancel()
            assertTrue(shortJob.isCancelled)
            assertTrue(longJob.isActive)
        }
        assertTrue(longJob.isCompleted)
        assertFalse(longJob.isCancelled)
        assertEquals(5, counter.load())
    }

    @Test
    fun longCancel() = runTest {
        val counter = AtomicInt(0)
        val shortJob: Job
        val longJob: Job
        coroutineScope {
            shortJob = launch { counter.delay(2000, 3) }
            longJob = launch { counter.delay(3000, 999) }
            counter.delay(2500, 4)
            longJob.cancel()
            assertTrue(shortJob.isCompleted)
            assertFalse(shortJob.isCancelled)
            assertTrue(longJob.isCancelled)
            // cancel is idempotent
            shortJob.cancel()
            longJob.cancel()
        }
        assertEquals(5, counter.load())
    }
}

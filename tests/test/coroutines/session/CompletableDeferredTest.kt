package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.coroutines.JobState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CompletableDeferredTest {
    @Test
    fun complete() = runTest {
        val deferred = CompletableDeferred<Int>()
        JobState.Active.assert(deferred)
        val result = async { deferred.await() }
        assertTrue(deferred.complete(123))
        JobState.Completed.assert(deferred)
        assertEquals(123, result.await())
    }

    @Test
    fun cancel() = runTest {
        val deferred = CompletableDeferred<Int>()
        JobState.Active.assert(deferred)
        val result = async { deferred.await() }
        deferred.cancel()
        JobState.Cancelled.assert(deferred)
        assertFailsWith<CancellationException> { result.await() }
    }

    @Test
    fun parent() = runTest {
        val parent = Job()
        val deferred = CompletableDeferred<Int>(parent)
        JobState.Active.assert(parent)
        JobState.Active.assert(deferred)
        parent.cancel()
        JobState.Cancelled.assert(parent)
        JobState.Cancelled.assert(deferred)
    }
}

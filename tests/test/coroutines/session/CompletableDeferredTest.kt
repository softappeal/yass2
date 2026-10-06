package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.coroutines.JobState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompletableDeferredTest {
    @Test
    fun test() = runTest {
        val job = currentCoroutineContext()[Job]!!
        val deferred = CompletableDeferred<Int>(job)
        JobState.Active.assert(deferred)
        assertTrue(deferred.complete(123))
        JobState.Completed.assert(deferred)
        assertEquals(123, deferred.await())
        JobState.Completed.assert(deferred)
        deferred.cancel()
        JobState.Completed.assert(deferred)
    }
}

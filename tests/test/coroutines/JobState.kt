package ch.softappeal.yass2.coroutines

import kotlinx.coroutines.Job
import kotlin.test.assertEquals

@Suppress("unused")
enum class JobState(private val active: Boolean, private val completed: Boolean, private val cancelled: Boolean) {
    New(active = false, completed = false, cancelled = false),       // optional initial state
    Active(active = true, completed = false, cancelled = false),     // default initial state
    Completing(active = true, completed = false, cancelled = false), // transient state
    Cancelling(active = false, completed = false, cancelled = true), // transient state
    Cancelled(active = false, completed = true, cancelled = true),   // final state
    Completed(active = false, completed = true, cancelled = false),  // final state
    ;

    fun assert(job: Job) {
        assertEquals(active, job.isActive)
        assertEquals(completed, job.isCompleted)
        assertEquals(cancelled, job.isCancelled)
    }
}

package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.coroutines.JobState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class ChannelTest {
    @Test
    fun rendezvous() = runTest {
        val channel = Channel<Int>()
        val received = async { channel.receive() }
        JobState.Active.assert(received)
        channel.send(123)
        JobState.Completed.assert(received)
        assertEquals(123, received.await())
    }

    @Test
    fun cancelSend() = runTest {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            val channel = Channel<Int>()
            val started = TimeSource.Monotonic.markNow()
            launch {
                delay(100.milliseconds)
                channel.cancel()
            }
            assertFailsWith<CancellationException> { channel.send(123) }
            assertTrue(started.elapsedNow().inWholeMilliseconds >= 100)
        }
    }

    @Test
    fun cancelReceive() = runTest {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            val channel = Channel<Int>()
            val started = TimeSource.Monotonic.markNow()
            launch {
                delay(100.milliseconds)
                channel.cancel()
            }
            assertFailsWith<CancellationException> { channel.receive() }
            assertTrue(started.elapsedNow().inWholeMilliseconds >= 100)
        }
    }
}

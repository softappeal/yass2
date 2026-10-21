package ch.softappeal.yass2.remote.session

import ch.softappeal.yass2.remote.CalculatorId
import ch.softappeal.yass2.remote.CalculatorImpl
import ch.softappeal.yass2.remote.JobState
import ch.softappeal.yass2.remote.proxy
import ch.softappeal.yass2.remote.service
import ch.softappeal.yass2.remote.tunnel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private suspend fun keepAliveTest(
    timeout: Duration,
    interval: Duration,
    keepAliveFun: suspend () -> Unit,
    open: suspend Session<Connection>.() -> Unit,
) {
    connect(
        {
            object : Session<Connection>() {
                override fun opened() {
                    launch {
                        assertFalse(isClosedSuspend())
                        launchKeepAlive(timeout, interval)
                        open()
                        assertTrue(isClosedSuspend())
                    }
                }

                override suspend fun closed(e: Exception?) = println("session1 closed: $e")
            }
        },
        {
            object : Session<Connection>() {
                override val serverTunnel = tunnel(KeepAliveId.service(object : KeepAlive {
                    override suspend fun keepAlive() {
                        println("keepAlive")
                        keepAliveFun()
                    }
                }))

                override suspend fun closed(e: Exception?) = println("session2 closed: $e")
            }
        },
    )
}

class KeepAliveTest {
    @OptIn(ExperimentalAtomicApi::class)
    @Test
    fun keepAliveClose() = runTest {
        val counter = AtomicInt(0)
        keepAliveTest(100.milliseconds, 200.milliseconds, { counter.incrementAndFetch() }) {
            delay(450.milliseconds)
            close()
            delay(200.milliseconds)
            JobState.Completed.assert(coroutineContext[Job]!!)
            assertEquals(3, counter.load())
        }
    }

    @Test
    fun keepAliveException() = runTest {
        keepAliveTest(100.milliseconds, 200.milliseconds, { throw Exception("keepAlive") }) {
            delay(50.milliseconds)
            JobState.Completed.assert(coroutineContext[Job]!!)
        }
    }

    @Test
    fun keepAliveTimeout() = runTest {
        keepAliveTest(100.milliseconds, 200.milliseconds, { delay(150.milliseconds) }) {
            delay(200.milliseconds)
            JobState.Completed.assert(coroutineContext[Job]!!)
        }
    }

    @Test
    fun keepAliveTunnel() = runTest {
        val tunnel = keepAliveTunnel(CalculatorId.service(CalculatorImpl))
        assertEquals(3, CalculatorId.proxy(tunnel).add(1, 2))
        KeepAliveId.proxy(tunnel).keepAlive()
    }
}

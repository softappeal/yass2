package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.CalculatorId
import ch.softappeal.yass2.core.CalculatorImpl
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.proxy
import ch.softappeal.yass2.service
import kotlinx.coroutines.CoroutineScope
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
import kotlin.time.Duration.Companion.milliseconds

private suspend fun CoroutineScope.keepAliveTest(keepAliveFun: suspend () -> Unit, open: suspend Session<Connection>.() -> Unit) {
    connect(
        {
            object : Session<Connection>() {
                override fun opened() {
                    launch {
                        assertFalse(isClosed())
                        open()
                        assertTrue(isClosed())
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

@OptIn(ExperimentalAtomicApi::class)
class KeepAliveTest {
    @Test
    fun keepAliveClose() = runTest {
        val counter = AtomicInt(0)
        keepAliveTest({ counter.incrementAndFetch() }) {
            val job = launchKeepAlive(this, 100.milliseconds, 200.milliseconds)
            delay(450.milliseconds)
            close()
            delay(200.milliseconds)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
            assertEquals(3, counter.load())
        }
    }

    @Test
    fun keepAliveCancel() = runTest {
        val counter = AtomicInt(0)
        keepAliveTest({ counter.incrementAndFetch() }) {
            val job = launchKeepAlive(this, 100.milliseconds, 200.milliseconds)
            delay(450.milliseconds)
            job.cancel()
            delay(50.milliseconds)
            assertTrue(job.isCompleted)
            assertTrue(job.isCancelled)
            assertEquals(3, counter.load())
        }
    }

    @Test
    fun keepAliveException() = runTest {
        keepAliveTest({ throw Exception("keepAlive") }) {
            val job = launchKeepAlive(this, 100.milliseconds, 200.milliseconds)
            delay(50.milliseconds)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }
    }

    @Test
    fun keepAliveTimeout() = runTest {
        keepAliveTest({ delay(150.milliseconds) }) {
            val job = launchKeepAlive(this, 100.milliseconds, 200.milliseconds)
            delay(200.milliseconds)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }
    }

    @Test
    fun keepAliveTunnel() = runTest {
        val tunnel = keepAliveTunnel(CalculatorId.service(CalculatorImpl))
        assertEquals(3, CalculatorId.proxy(tunnel).add(1, 2))
        KeepAliveId.proxy(tunnel).keepAlive()
    }
}

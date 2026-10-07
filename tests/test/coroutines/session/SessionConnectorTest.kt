package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.CalculatorId
import ch.softappeal.yass2.core.CalculatorImpl
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.proxy
import ch.softappeal.yass2.service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@OptIn(ExperimentalAtomicApi::class)
class SessionConnectorTest {
    @Test
    fun test() = runTest {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            val opened = AtomicInt(0)
            val initiatorSessionFactory = {
                object : Session<Connection>() {
                    override fun opened() {
                        launch {
                            opened.incrementAndFetch()
                            println("opened ${CalculatorId.proxy(clientTunnel).add(0, opened.load())}")
                            delay((if (opened.load() == 3) 300 else 100).milliseconds)
                            close()
                        }
                    }

                    override suspend fun closed(e: Exception?) {
                        assertNull(e)
                        println("initiatorSession closed $opened")
                    }
                }
            }
            val acceptorSessionFactory = {
                object : Session<Connection>() {
                    override val serverTunnel = tunnel(CalculatorId.service(CalculatorImpl))
                    override suspend fun closed(e: Exception?) {
                        assertNull(e)
                        println("acceptorSession closed $opened")
                    }
                }
            }
            val started = TimeSource.Monotonic.markNow()
            val job = launchConnector(
                initiatorSessionFactory,
                200.milliseconds,
            ) {
                println()
                println("connecting at ${started.elapsedNow().inWholeMilliseconds}ms")
                if (opened.load() == 5) throw Exception("connect failed")
                launch { connect(it, acceptorSessionFactory) }
            }
            delay(500.milliseconds)
            assertEquals(3, opened.load()) // 500ms
            delay(200.milliseconds)
            assertEquals(3, opened.load()) // 700ms
            delay(200.milliseconds)
            assertEquals(4, opened.load()) // 900ms
            delay(200.milliseconds)
            assertEquals(5, opened.load()) // 1100ms
            delay(200.milliseconds)
            assertEquals(5, opened.load()) // 1300ms
            opened.incrementAndFetch()
            delay(200.milliseconds)
            assertEquals(7, opened.load()) // 1500ms
            delay(200.milliseconds)
            assertEquals(8, opened.load()) // 1700ms
            job.cancel()
            delay(200.milliseconds)
            assertEquals(8, opened.load()) // 1900ms
        }
    }
}

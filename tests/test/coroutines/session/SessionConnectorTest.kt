package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.CalculatorId
import ch.softappeal.yass2.core.CalculatorImpl
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.proxy
import ch.softappeal.yass2.service
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalAtomicApi::class)
class SessionConnectorTest {
    @Test
    fun test() = runTest {
        val counter = AtomicInt(0)
        val initiatorSessionFactory = {
            object : Session<Connection>() {
                override fun opened() {
                    launch {
                        counter.incrementAndFetch()
                        println(CalculatorId.proxy(clientTunnel).add(0, counter.load()))
                        delay((if (counter.load() == 19) 1000 else 100).milliseconds)
                        close()
                    }
                }

                override suspend fun closed(e: Exception?) {
                    counter.incrementAndFetch()
                    assertNull(e)
                    println("initiatorSession closed $counter")
                }
            }
        }
        val acceptorSessionFactory = {
            object : Session<Connection>() {
                override val serverTunnel = tunnel(CalculatorId.service(CalculatorImpl))
                override suspend fun closed(e: Exception?) {
                    counter.incrementAndFetch()
                    assertNull(e)
                    println("acceptorSession closed $counter")
                }
            }
        }
        val job = launchConnector(
            initiatorSessionFactory,
            200.milliseconds,
        ) {
            counter.incrementAndFetch()
            println("connect $counter")
            if (counter.load() == 9) throw Exception("connect failed")
            launch { connect(it, acceptorSessionFactory) }
        }
        delay(1300.milliseconds)
        assertEquals(19, counter.load())
        delay(600.milliseconds)
        assertEquals(19, counter.load())
        delay(500.milliseconds)
        job.cancel()
        assertEquals(29, counter.load())
    }
}

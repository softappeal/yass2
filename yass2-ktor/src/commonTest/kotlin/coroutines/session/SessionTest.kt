package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.EchoId
import ch.softappeal.yass2.core.EchoImpl
import ch.softappeal.yass2.core.remote.serverServices
import ch.softappeal.yass2.core.remote.test
import ch.softappeal.yass2.proxy
import ch.softappeal.yass2.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

fun <C : Connection> CoroutineScope.acceptorSessionFactory(context: suspend Session<C>.() -> Any): SessionFactory<C> = {
    object : Session<C>() {
        override val serverServices = serverServices { context() }

        override fun opened() {
            launch {
                val echo = EchoId.proxy(clientTunnel)
                val value = "echo from acceptor"
                val result = echo.echo(value)
                println("<$result>")
                assertEquals(value, result)
            }
        }

        override suspend fun closed(e: Exception?) {
            assertTrue(isClosed())
            println("acceptorSessionFactory closed: $e")
            e?.printStackTrace()
            assertNull(e)
        }
    }
}

fun <C : Connection> CoroutineScope.initiatorSessionFactory(): SessionFactory<C> = {
    object : Session<C>() {
        override val serverServices = listOf(EchoId.service(EchoImpl))

        override fun opened() {
            launch {
                assertFalse(isClosed())
                clientTunnel.test()
                close()
                assertTrue(isClosed())
            }
        }

        override suspend fun closed(e: Exception?) {
            assertTrue(isClosed())
            println("initiatorSessionFactory closed: $e")
            e?.printStackTrace()
            assertNull(e)
        }
    }
}

private suspend fun connect(
    sessionFactory1: SessionFactory<Connection>,
    sessionFactory2: SessionFactory<Connection>,
) {
    class LocalConnection(private val channel: Channel<Packet?>) : Connection {
        override suspend fun write(packet: Packet?) {
            channel.send(packet)
        }

        override suspend fun closed() {
            channel.close()
        }
    }

    val channel1 = Channel<Packet?>(1)
    val channel2 = Channel<Packet?>(1)
    val connection1 = LocalConnection(channel1)
    val connection2 = LocalConnection(channel2)

    coroutineScope {
        launch {
            connection1.receiveLoop(sessionFactory1) { channel2.receive() }
        }
        launch {
            connection2.receiveLoop(sessionFactory2) { channel1.receive() }
        }
    }
}

class SessionTest {
    @Test
    fun test() = runTest {
        connect(initiatorSessionFactory(), acceptorSessionFactory { connection })
    }

    @OptIn(ExperimentalAtomicApi::class)
    @Test
    fun cancel() = runTest {
        val counter = AtomicInt(0)

        abstract class MySession : Session<Connection>() {
            override suspend fun closed(e: Exception?) {
                assertNull(e)
                counter.incrementAndFetch()
                println("closed $this")
            }
        }

        connect(
            {
                object : MySession() {
                    override fun opened() {
                        val echo = EchoId.proxy(clientTunnel)
                        launch {
                            assertFailsWith<TimeoutCancellationException> {
                                withTimeout(5_000) { echo.delay(10_000) }
                            }
                            close()
                        }
                    }
                }
            },
            {
                object : MySession() {
                    override val serverServices = listOf(EchoId.service(EchoImpl.proxy { _, _, invoke ->
                        assertEquals(1, counter.incrementAndFetch())
                        invoke()
                    }))
                }
            },
        )

        delay(20_000)
        assertEquals(3, counter.load())
    }

    @Test
    fun timedOutResume() = runTest {
        lateinit var continuation: Continuation<String>
        suspend fun getString(): String = suspendCancellableCoroutine { c: Continuation<String> ->
            continuation = c
            println(continuation)
            CoroutineScope(continuation.context).launch { delay(200.milliseconds) }
        }
        launch {
            delay(100.milliseconds)
            continuation.resume("hello")
        }
        assertEquals("hello", getString())
        assertFailsWith<TimeoutCancellationException> {
            withTimeout(100) { getString() }
        }
        continuation.resume("first call after withTimeout")
        println(assertFailsWith<Exception> { continuation.resume("second call after withTimeout") })
        println("done")
    }

    @Test
    fun heartbeat() = runTest {
        val sessionFactory1 = {
            object : Session<Connection>(HeartbeatConfig(200, 40)) {
                override fun opened() {
                    launch {
                        delay(5000.milliseconds)
                        close()
                    }
                }

                override suspend fun closed(e: Exception?) = println("session1 closed: $e")
            }
        }

        val sessionFactory2 = {
            var timeoutMillis: Long = 20
            object : Session<Connection>(
                null,
                object : Heartbeat {
                    override suspend fun echo() {
                        println("check")
                        delay(timeoutMillis)
                        timeoutMillis += 4
                    }
                },
            ) {
                override suspend fun closed(e: Exception?) = println("session2 closed: $e")
            }
        }
        connect(sessionFactory1, sessionFactory2)
    }

    @Test
    fun connect() = runTest {
        val initiatorSessionFactory = {
            object : Session<Connection>() {
                override fun opened() {
                    launch {
                        val echo = EchoId.proxy(clientTunnel)
                        println(echo.echo("hello"))
                        close()
                    }
                }

                override suspend fun closed(e: Exception?) = println("initiatorSession closed: $e")
            }
        }

        val acceptorSessionFactory = {
            object : Session<Connection>() {
                override val serverServices = listOf(EchoId.service(EchoImpl))
                override suspend fun closed(e: Exception?) = println("acceptorSession closed: $e")
            }
        }
        val job = connect(initiatorSessionFactory, 200) { connect(it, acceptorSessionFactory) }
        delay(600.milliseconds)
        job.cancel()
    }
}

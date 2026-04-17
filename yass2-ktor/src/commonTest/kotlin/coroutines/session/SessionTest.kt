package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.CalculatorId
import ch.softappeal.yass2.EchoId
import ch.softappeal.yass2.core.CalculatorImpl
import ch.softappeal.yass2.core.EchoImpl
import ch.softappeal.yass2.core.remote.clientTest
import ch.softappeal.yass2.core.remote.serverTunnel
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.proxy
import ch.softappeal.yass2.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

fun <C : Connection> CoroutineScope.acceptorSessionFactory(context: suspend Session<C>.() -> Any): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = serverTunnel { context() }

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
            assertNull(e)
            println("acceptorSession closed")
        }
    }
}

fun <C : Connection> CoroutineScope.initiatorSessionFactory(): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = tunnel(EchoId.service(EchoImpl))

        override fun opened() {
            launch {
                assertFalse(isClosed())
                clientTunnel.clientTest()
                close()
                assertTrue(isClosed())
            }
        }

        override suspend fun closed(e: Exception?) {
            assertTrue(isClosed())
            assertNull(e)
            println("initiatorSession closed")
        }
    }
}

private suspend fun localConnect(
    sessionFactory1: SessionFactory<Connection>,
    sessionFactory2: SessionFactory<Connection>,
) {
    class LocalConnection : Connection {
        val channel = Channel<Packet?>(1)
        override suspend fun write(packet: Packet?) = channel.send(packet)
        override suspend fun closed() {
            channel.close()
        }
    }

    val connection1 = LocalConnection()
    val connection2 = LocalConnection()
    coroutineScope {
        launch {
            connection1.receiveLoop(sessionFactory1) { connection2.channel.receive() }
        }
        launch {
            connection2.receiveLoop(sessionFactory2) { connection1.channel.receive() }
        }
    }
}

private suspend fun CoroutineScope.keepAliveTest(keepAliveFun: suspend () -> Unit, open: suspend Session<Connection>.() -> Unit) {
    localConnect(
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
class SessionTest {
    @Test
    fun invoke() = runTest {
        localConnect(initiatorSessionFactory(), acceptorSessionFactory { connection })
    }

    @Test
    fun keepAliveClose() = runTest {
        val counter = AtomicInt(0)
        keepAliveTest({ counter.incrementAndFetch() }) {
            val job = launchKeepAlive(this, 100.milliseconds, 200.milliseconds)
            delay(450.milliseconds)
            close()
            delay(50.milliseconds)
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
        val tunnel = keepAliveTunnel(EchoId.service(EchoImpl), CalculatorId.service(CalculatorImpl))
        assertEquals("echo", EchoId.proxy(tunnel).echo("echo"))
        assertEquals(3, CalculatorId.proxy(tunnel).add(1, 2))
        KeepAliveId.proxy(tunnel).keepAlive()
    }

    @Test
    fun connector() = runTest {
        val counter = AtomicInt(0)
        val initiatorSessionFactory = {
            object : Session<Connection>() {
                override fun opened() {
                    launch {
                        counter.incrementAndFetch()
                        println(EchoId.proxy(clientTunnel).echo("opened $counter"))
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
                override val serverTunnel = tunnel(EchoId.service(EchoImpl))
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
            launch { localConnect(it, acceptorSessionFactory) }
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

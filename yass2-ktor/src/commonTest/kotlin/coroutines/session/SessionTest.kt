package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.Echo
import ch.softappeal.yass2.EchoId
import ch.softappeal.yass2.core.EchoImpl
import ch.softappeal.yass2.core.remote.test
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

fun <C : Connection> CoroutineScope.acceptorSessionFactory(context: suspend Session<C>.() -> Any): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = tunnel { context() }

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
            println("acceptorSessionFactory closed")
        }
    }
}

fun <C : Connection> CoroutineScope.initiatorSessionFactory(): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = tunnel(EchoId.service(EchoImpl))

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
            assertNull(e)
            println("initiatorSessionFactory closed")
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

private suspend fun CoroutineScope.heartbeatTest(open: suspend Session<Connection>.(echo: Echo) -> Unit) {
    localConnect(
        {
            object : Session<Connection>() {
                override fun opened() {
                    launch {
                        assertFalse(isClosed())
                        open(EchoId.proxy(clientTunnel))
                        assertTrue(isClosed())
                    }
                }

                override suspend fun closed(e: Exception?) = println("session1 closed: $e")
            }
        },
        {
            object : Session<Connection>() {
                override val serverTunnel = tunnel(EchoId.service(EchoImpl))
                override suspend fun closed(e: Exception?) = println("session2 closed: $e")
            }
        },
    )
}

@OptIn(ExperimentalAtomicApi::class)
class SessionTest {
    @Test
    fun test() = runTest {
        localConnect(initiatorSessionFactory(), acceptorSessionFactory { connection })
    }

    @Test
    fun heartbeatClose() = runTest {
        heartbeatTest { echo ->
            val job = heartbeat(200, 100) {
                println("heartbeat")
                echo.noParametersNoResult()
            }
            delay(500)
            close()
            delay(400)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }
    }

    @Test
    fun heartbeatCancel() = runTest {
        heartbeatTest {
            val job = heartbeat(200, 100) { println("heartbeat") }
            delay(500)
            job.cancel()
            delay(100)
            assertTrue(job.isCompleted)
            assertTrue(job.isCancelled)
        }
    }

    @Test
    fun heartbeatException() = runTest {
        heartbeatTest {
            val job = heartbeat(200, 100) { throw Exception("heartbeat") }
            delay(100)
            assertTrue(job.isCompleted)
            assertFalse(job.isCancelled)
        }
    }

    @Test
    fun heartbeatTimeout() = runTest {
        heartbeatTest {
            var timeoutMillis = 0L
            val job = heartbeat(200, 100) {
                println("heartbeat")
                delay(timeoutMillis)
                @Suppress("AssignedValueIsNeverRead")
                timeoutMillis += 75
            }
            delay(700)
            assertTrue(job.isCompleted)
            assertTrue(job.isCancelled)
        }
    }

    @Test
    fun connect() = runTest {
        val counter = AtomicInt(0)
        val initiatorSessionFactory = {
            object : Session<Connection>() {
                override fun opened() {
                    launch {
                        counter.incrementAndFetch()
                        println(EchoId.proxy(clientTunnel).echo("opened $counter"))
                        delay(if (counter.load() == 19) 1000 else 100)
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
        val job = connect(
            initiatorSessionFactory,
            200,
        ) {
            counter.incrementAndFetch()
            println("connect $counter")
            if (counter.load() == 9) throw Exception("connect failed")
            launch { localConnect(it, acceptorSessionFactory) }
        }
        delay(1300)
        assertEquals(19, counter.load())
        delay(600)
        assertEquals(19, counter.load())
        delay(500)
        job.cancel()
        assertEquals(29, counter.load())
    }
}

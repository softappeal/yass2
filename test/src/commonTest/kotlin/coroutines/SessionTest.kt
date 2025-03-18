package ch.softappeal.yass2.coroutines

import ch.softappeal.yass2.CalculatorImpl
import ch.softappeal.yass2.EchoImpl
import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.assertSuspendFailsWith
import ch.softappeal.yass2.contract.CalculatorId
import ch.softappeal.yass2.contract.EchoId
import ch.softappeal.yass2.contract.proxy
import ch.softappeal.yass2.contract.service
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.remote.tunnel
import ch.softappeal.yass2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

fun tunnel(context: suspend () -> Any): Tunnel = tunnel(
    CalculatorId.service(CalculatorImpl),
    EchoId.service(EchoImpl.proxy { _, _, invoke ->
        println("context<${context()}>")
        invoke()
    }),
)

suspend fun Tunnel.test() {
    test(CalculatorId.proxy(this), EchoId.proxy(this))
}

fun <C : Connection> CoroutineScope.acceptorSessionFactory(context: suspend Session<C>.() -> Any): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = tunnel { context() }

        override fun opened() {
            launch {
                val echo = EchoId.proxy(clientTunnel)
                val value = "echo from acceptor"
                val result = echo.echo(value)
                print("<$result>")
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
            println("initiatorSessionFactory closed: $e")
            e?.printStackTrace()
            assertNull(e)
        }
    }
}

@OptIn(InternalApi::class)
class SessionTest {
    @Test
    fun test() = runTest {
        connect(initiatorSessionFactory<Connection>()(), acceptorSessionFactory { connection }())
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
        assertSuspendFailsWith<TimeoutCancellationException> {
            withTimeout(100) { getString() }
        }
        continuation.resume("first call after withTimeout")
        println(assertFailsWith<Exception> { continuation.resume("second call after withTimeout") })
        println("done")
    }

    @Test
    fun watch() = runTest {
        val session1 = object : Session<Connection>() {
            override fun opened() {
                val session = this
                launch {
                    val echo = EchoId.proxy(clientTunnel)
                    var timeoutMillis = 20
                    val job = watch(session, 200, 40) {
                        println("check")
                        echo.delay(timeoutMillis)
                        timeoutMillis += 4
                    }
                    println(echo.echo("hello"))
                    println(job)
                }
            }

            override suspend fun closed(e: Exception?) = println("session1 closed: $e")
        }
        val session2 = object : Session<Connection>() {
            override val serverTunnel = tunnel(EchoId.service(EchoImpl))
            override suspend fun closed(e: Exception?) = println("session2 closed: $e")
        }
        connect(session1, session2)
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

        val serverTunnel = tunnel(EchoId.service(EchoImpl))
        val acceptorSessionFactory = {
            object : Session<Connection>() {
                override val serverTunnel = serverTunnel
                override suspend fun closed(e: Exception?) = println("acceptorSession closed: $e")
            }
        }
        val job = connect(initiatorSessionFactory, 200) { connect(it(), acceptorSessionFactory()) }
        delay(600.milliseconds)
        job.cancel()
    }
}

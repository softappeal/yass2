package ch.softappeal.yass2.remote.coroutines.session

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.coroutines.*
import kotlin.test.*

fun tunnel(context: suspend () -> Any): Tunnel = ::generatedInvoke.tunnel(
    CalculatorId(CalculatorImpl),
    EchoId(GeneratedProxyFactory(EchoImpl) { _, _, invocation: SuspendInvocation ->
        println("context<${context()}>")
        invocation()
    }),
    FlowServiceId(FlowServiceImpl),
)

suspend fun Tunnel.test(iterations: Int): Unit = with(generatedRemoteProxyFactory(this)) {
    val calculator = this(CalculatorId)
    GeneratedProxyFactory.test(calculator, this(EchoId))
    performance(iterations) { assertEquals(5, calculator.add(2, 3)) }
    this(FlowServiceId).test()
}

fun CoroutineScope.acceptorSessionFactory(context: suspend Session.() -> Any): SessionFactory = {
    object : Session() {
        override val serverTunnel = tunnel { context() }

        override fun opened() {
            val ct = clientTunnel
            // If we move the line above into the block below, we get an IllegalAccessError on linux/mac if Ktor > 1.5.2:
            //   trying to access protected method 'clientTunnel' ('opened' is in unnamed module of loader io.ktor.server.engine.OverridingClassLoader; 'Session' is in unnamed module of loader 'app')
            launch {
                val echo = generatedRemoteProxyFactory(ct)(EchoId)
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

fun CoroutineScope.initiatorSessionFactory(iterations: Int): SessionFactory = {
    object : Session() {
        override val serverTunnel = ::generatedInvoke.tunnel(EchoId(EchoImpl))

        override fun opened() {
            launch {
                assertFalse(isClosed())
                clientTunnel.test(iterations)
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

private fun connect(session1: Session, session2: Session) {
    class LocalConnection(val session: Session) : Connection {
        override suspend fun write(packet: Packet?) = session.received(packet)
        override suspend fun closed() = session.close()
    }
    session1.connection = LocalConnection(session2)
    session2.connection = LocalConnection(session1)
    session1.opened()
    session2.opened()
}

class SessionTest {
    @Test
    fun test() = runTest {
        connect(initiatorSessionFactory(1000)(), acceptorSessionFactory { connection }())
    }

    @Test
    fun timedOutResume() = runTest {
        lateinit var continuation: Continuation<String>
        suspend fun getString(): String = suspendCancellableCoroutine { c: Continuation<String> ->
            continuation = c
            println(continuation)
            CoroutineScope(continuation.context).launch { delay(200) }
        }
        launch {
            delay(100)
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
        val session1 = object : Session() {
            override fun opened() {
                launch {
                    val echo = generatedRemoteProxyFactory(clientTunnel)(EchoId)
                    var timeout = 20
                    val job = watch(200, 40) {
                        println("check")
                        echo.delay(timeout)
                        timeout += 4
                    }
                    println(echo.echo("hello"))
                    println(job)
                }
            }

            override suspend fun closed(e: Exception?) = println("session1 closed: $e")
        }
        val serverTunnel = ::generatedInvoke.tunnel(EchoId(EchoImpl))
        val session2 = object : Session() {
            override val serverTunnel = serverTunnel
            override suspend fun closed(e: Exception?) = println("session2 closed: $e")
        }
        connect(session1, session2)
    }

    @Test
    fun connect() = runTest {
        val initiatorSessionFactory = {
            object : Session() {
                override fun opened() {
                    launch {
                        val echo = generatedRemoteProxyFactory(clientTunnel)(EchoId)
                        println(echo.echo("hello"))
                        close()
                    }
                }

                override suspend fun closed(e: Exception?) = println("initiatorSession closed: $e")
            }
        }

        val serverTunnel = ::generatedInvoke.tunnel(EchoId(EchoImpl))
        val acceptorSessionFactory = {
            object : Session() {
                override val serverTunnel = serverTunnel
                override suspend fun closed(e: Exception?) = println("acceptorSession closed: $e")
            }
        }
        val job = connect(initiatorSessionFactory, 200) { connect(it(), acceptorSessionFactory()) }
        delay(600)
        job.cancel()
    }
}

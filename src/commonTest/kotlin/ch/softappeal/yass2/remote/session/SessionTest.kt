package ch.softappeal.yass2.remote.session

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.remote.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import kotlin.test.*

fun CoroutineScope.acceptorSessionFactory(context: suspend Session.() -> Any): SessionFactory = {
    object : Session() {
        override val serverTunnel = tunnel { context() }

        override fun opened() {
            launch {
                val echo = generatedRemoteProxyFactoryCreator(clientTunnel)(EchoId)
                val value = "echo from acceptor"
                val result = echo.echo(value)
                print("<$result>")
                assertEquals(value, result)
            }
        }

        override suspend fun closed(e: Exception?) {
            assertTrue(isClosed())
            println("acceptorSessionFactory closed: $e")
        }
    }
}

fun CoroutineScope.initiatorSessionFactory(iterations: Int): SessionFactory = {
    object : Session() {
        override val serverTunnel = ::generatedInvoker.tunnel(listOf(EchoId(EchoImpl)))

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
        }
    }
}

class SessionTest {
    @Test
    fun test() = yassRunBlocking {
        fun connect(session1: Session, session2: Session) {
            class LocalConnection(val session: Session) : Connection {
                override suspend fun write(packet: Packet?) = session.received(packet)
                override suspend fun closed() = session.close()
            }
            session1.connection = LocalConnection(session2)
            session2.connection = LocalConnection(session1)
            session1.opened()
            session2.opened()
        }
        connect(initiatorSessionFactory(1000)(), acceptorSessionFactory { connection }())
    }

    @Test
    fun timedOutResume() = yassRunBlocking {
        lateinit var continuation: Continuation<String>
        suspend fun getString(): String = suspendCancellableCoroutine { c: Continuation<String> ->
            continuation = c
            println(continuation)
            CoroutineScope(continuation.context).launch { delay(200) }
            COROUTINE_SUSPENDED
        }
        launch {
            delay(100)
            continuation.resume("hello")
        }
        assertEquals("hello", getString())
        try {
            withTimeout(100) { getString() }
            fail()
        } catch (ignore: TimeoutCancellationException) {
        }
        try {
            continuation.resume("first call after withTimeout")
        } catch (e: Exception) {
            fail()
        }
        try {
            continuation.resume("second call after withTimeout")
            fail()
        } catch (e: Exception) {
            println(e)
        }
        println("done")
    }
}

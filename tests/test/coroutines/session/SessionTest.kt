package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.core.TestMode
import ch.softappeal.yass2.core.remote.clientTest
import ch.softappeal.yass2.core.remote.serverTunnel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

suspend fun connect(
    sessionFactory1: SessionFactory<Connection>,
    sessionFactory2: SessionFactory<Connection>,
) {
    class LocalConnection : Connection {
        val channel = Channel<Packet?>(1)
        override suspend fun write(packet: Packet?) = channel.send(packet)
        override suspend fun closed() {
            channel.cancel()
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

const val INITIATOR = "initiator"
const val ACCEPTOR = "acceptor"

fun <C : Connection> CoroutineScope.sessionFactory(
    testMode: TestMode,
    runTests: Boolean,
    type: String,
    context: Session<C>.() -> Unit = {},
): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = serverTunnel("$type.server") { _, _, invocation ->
            context()
            invocation()
        }

        override fun opened() {
            launch {
                println("$type session opened")
                assertFalse(isClosed())
                if (runTests) {
                    clientTunnel.clientTest(testMode, "$type.client")
                    close()
                    assertTrue(isClosed())
                }
            }
        }

        override suspend fun closed(e: Exception?) {
            println("$type session closed: $e")
            assertTrue(isClosed())
            if (testMode == TestMode.Normal) assertNull(e)
        }
    }
}

class SessionTest {
    @Test
    fun test1() = runTest {
        connect(
            sessionFactory(TestMode.Normal, runTests = true, INITIATOR),
            sessionFactory(TestMode.Normal, runTests = false, ACCEPTOR),
        )
    }

    @Test
    fun test12() = runTest {
        connect(
            sessionFactory(TestMode.Normal, runTests = true, INITIATOR),
            sessionFactory(TestMode.Normal, runTests = true, ACCEPTOR),
        )
    }

    @Test
    fun testException1() = runTest {
        connect(
            sessionFactory(TestMode.Exception, runTests = true, INITIATOR),
            sessionFactory(TestMode.Exception, runTests = false, ACCEPTOR),
        )
    }
}

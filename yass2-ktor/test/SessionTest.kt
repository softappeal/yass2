package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.remote.session.Connection
import ch.softappeal.yass2.remote.session.Session
import ch.softappeal.yass2.remote.session.SessionFactory
import kotlinx.coroutines.launch
import kotlin.test.assertTrue

const val INITIATOR = "initiator"
const val ACCEPTOR = "acceptor"

fun <C : Connection> sessionFactory(
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
                if (!runTests) return@launch
                clientTunnel.clientTest(testMode, "$type.client")
                close()
                assertTrue(isClosedSuspend())
            }
        }

        override suspend fun closed(e: Exception?) {
            println("$type session closed: $e")
            assertTrue(isClosedSuspend())
        }
    }
}

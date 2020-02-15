package ch.softappeal.yass2.remote.session

import kotlinx.coroutines.*

typealias SessionConnector = suspend (sessionFactory: SessionFactory) -> Unit

/**
 * Launches a new coroutine that maintains a session.
 * [checkAlive] must execute without an exception within [checkAliveTimeoutMillis] if session is alive.
 */
fun <S : Session> CoroutineScope.connect(
    reconnectDelayMillis: Long = 10_000,
    checkAliveTimeoutMillis: Long = 1000, checkAlive: suspend (session: S) -> Unit = {},
    sessionFactory: () -> S, sessionConnector: SessionConnector
): Job {
    require(reconnectDelayMillis > 0)
    require(checkAliveTimeoutMillis > 0)
    suspend fun S.isDead(): Boolean {
        try {
            withTimeout(checkAliveTimeoutMillis) { checkAlive(this@isDead) }
            return false
        } catch (e: Exception) {
            try {
                close(e)
            } finally {
                return true
            }
        }
    }
    return launch {
        var session: S? = null
        while (isActive) {
            if (session == null || session.isClosed() || session.isDead()) {
                try {
                    sessionConnector {
                        val s = sessionFactory()
                        session = s
                        s
                    }
                } catch (ignore: Exception) {
                }
            }
            delay(reconnectDelayMillis)
        }
    }
}

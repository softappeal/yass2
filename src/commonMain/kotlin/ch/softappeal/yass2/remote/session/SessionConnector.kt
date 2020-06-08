package ch.softappeal.yass2.remote.session

import kotlinx.coroutines.*

public typealias SessionConnector = suspend (sessionFactory: SessionFactory) -> Unit

/** Launches a new coroutine that maintains a session. */
public fun CoroutineScope.connect(
    sessionFactory: SessionFactory, intervalMillis: Long = 10_000, sessionConnector: SessionConnector
): Job {
    require(intervalMillis > 0)
    return launch {
        var session: Session? = null
        while (isActive) {
            if (session == null || session.isClosed()) {
                try {
                    sessionConnector {
                        sessionFactory().apply { session = this }
                    }
                } catch (ignore: Exception) {
                }
            }
            delay(intervalMillis)
        }
    }
}

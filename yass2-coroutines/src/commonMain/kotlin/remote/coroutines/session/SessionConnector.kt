package ch.softappeal.yass2.remote.coroutines.session

import kotlinx.coroutines.*

public typealias SessionConnector<C> = suspend (sessionFactory: SessionFactory<C>) -> Unit

/** Launches a new coroutine that maintains a session. */
public fun <C : Connection> CoroutineScope.connect(
    sessionFactory: SessionFactory<C>,
    intervalMillis: Long = 10_000,
    sessionConnector: SessionConnector<C>,
): Job {
    require(intervalMillis > 0)
    return launch {
        var session: Session<C>? = null
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

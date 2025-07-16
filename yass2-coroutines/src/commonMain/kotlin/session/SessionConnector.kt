package ch.softappeal.yass2.coroutines.session

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public typealias SessionConnector<C> = suspend (sessionFactory: SessionFactory<C>) -> Unit

/** Launches a new coroutine that maintains a session. */
public fun <C : Connection> CoroutineScope.connect(
    sessionFactory: SessionFactory<C>,
    intervalMillis: Long,
    sessionConnector: SessionConnector<C>,
): Job {
    require(intervalMillis > 0)
    return launch {
        var session: Session<C>? = null
        while (true) {
            if (session == null || session.isClosed()) {
                try {
                    sessionConnector {
                        sessionFactory().apply { session = this }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // ignore
                }
            }
            delay(intervalMillis)
        }
    }
}

package ch.softappeal.yass2.coroutines.session

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public typealias SessionConnector = suspend (sessionFactory: SessionFactory) -> Unit

/** Launches a new coroutine that maintains a session. */
public fun CoroutineScope.connect(
    sessionFactory: SessionFactory,
    intervalMillis: Long,
    sessionConnector: SessionConnector,
): Job {
    require(intervalMillis > 0)
    return launch {
        var session: Session? = null
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

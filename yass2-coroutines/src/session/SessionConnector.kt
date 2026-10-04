package ch.softappeal.yass2.coroutines.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

public typealias SessionConnector<C> = (sessionFactory: SessionFactory<C>) -> Unit

/** Launches a new coroutine that maintains a session. */
public fun <C : Connection> CoroutineScope.launchConnector(
    sessionFactory: SessionFactory<C>,
    interval: Duration,
    sessionConnector: SessionConnector<C>,
): Job = launch {
    var session: Session<C>? = null
    while (true) {
        if (session == null || session.isClosedSuspend()) {
            try {
                sessionConnector {
                    sessionFactory().apply { session = this }
                }
            } catch (_: Exception) {
                // ignore
            }
        }
        delay(interval)
    }
}

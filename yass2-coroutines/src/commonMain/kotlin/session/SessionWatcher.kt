package ch.softappeal.yass2.coroutines.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/** Launches a new coroutine that closes [session] if [check] throws an exception or doesn't return within [timeoutMillis]. */
public fun <C : Connection> CoroutineScope.watch(
    session: Session<C>,
    intervalMillis: Long = 10_000,
    timeoutMillis: Long = 1000,
    check: suspend () -> Unit,
): Job {
    require(intervalMillis > 0)
    require(timeoutMillis > 0)
    return launch {
        while (isActive) {
            try {
                withTimeout(timeoutMillis) { check() }
            } catch (e: Exception) {
                session.close(e)
                break
            }
            delay(intervalMillis)
        }
    }
}

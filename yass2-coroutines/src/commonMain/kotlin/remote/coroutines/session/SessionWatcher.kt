package ch.softappeal.yass2.remote.coroutines.session

import kotlinx.coroutines.*

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

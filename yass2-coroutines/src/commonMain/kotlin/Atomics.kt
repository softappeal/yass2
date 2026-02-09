package ch.softappeal.yass2.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// NOTE: replace with kotlin.concurrent.atomics if no longer ExperimentalAtomicApi

internal class AtomicBoolean(private var value: Boolean) {
    private val mutex = Mutex()
    suspend fun load(): Boolean = mutex.withLock { value }
    suspend fun exchange(value: Boolean): Boolean = mutex.withLock {
        val oldValue = this.value
        this.value = value
        oldValue
    }
}

internal class AtomicInt(private var value: Int) {
    private val mutex = Mutex()
    suspend fun incrementAndFetch(): Int = mutex.withLock { ++value }
}

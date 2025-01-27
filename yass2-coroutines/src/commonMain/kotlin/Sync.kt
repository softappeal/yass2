package ch.softappeal.yass2.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AtomicBoolean(private var value: Boolean) {
    private val mutex = Mutex()
    suspend fun get(): Boolean = mutex.withLock { value }
    suspend fun getAndSet(value: Boolean): Boolean = mutex.withLock {
        val oldValue = this.value
        this.value = value
        oldValue
    }
}

internal class AtomicInteger(private var value: Int) {
    private val mutex = Mutex()
    suspend fun incrementAndGet(): Int = mutex.withLock { ++value }
}

internal class ThreadSafeMap<K, V>(initialCapacity: Int) {
    private val mutex = Mutex()
    private val map = HashMap<K, V>(initialCapacity)
    suspend fun put(key: K, value: V): Unit = mutex.withLock { map[key] = value }
    suspend fun remove(key: K): V? = mutex.withLock { map.remove(key) }
}

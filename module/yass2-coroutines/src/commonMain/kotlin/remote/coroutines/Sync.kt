package ch.softappeal.yass2.remote.coroutines

import kotlinx.coroutines.sync.*

internal class AtomicBoolean(private var value: Boolean) {
    private val mutex = Mutex()
    suspend fun get(): Boolean = mutex.withLock { value }
    suspend fun getAndSet(value: Boolean): Boolean = mutex.withLock {
        val oldValue = this.value
        this.value = value
        oldValue
    }
}

public class AtomicInteger(private var value: Int) {
    private val mutex = Mutex()
    public suspend fun incrementAndGet(): Int = mutex.withLock { ++value }
}

public class ThreadSafeMap<K, V>(initialCapacity: Int) {
    private val mutex = Mutex()
    private val map = HashMap<K, V>(initialCapacity)
    public suspend fun get(key: K): V? = mutex.withLock { map[key] }
    public suspend fun put(key: K, value: V): Unit = mutex.withLock { map[key] = value }
    public suspend fun remove(key: K): V? = mutex.withLock { map.remove(key) }
}

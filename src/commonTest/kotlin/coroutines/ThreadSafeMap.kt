package ch.softappeal.yass2.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ThreadSafeMap<K, V>(initialCapacity: Int) {
    private val mutex = Mutex()
    private val map = HashMap<K, V>(initialCapacity)
    suspend fun put(key: K, value: V): Unit = mutex.withLock { map[key] = value }
    suspend fun remove(key: K): V? = mutex.withLock { map.remove(key) }
    suspend fun get(key: K): V? = mutex.withLock { map[key] }
}

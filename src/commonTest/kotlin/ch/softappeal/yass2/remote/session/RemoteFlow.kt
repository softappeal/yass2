package ch.softappeal.yass2.remote.session // TODO: move to main

import ch.softappeal.yass2.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

interface FlowService {
    suspend fun create(flowId: Any): Int
    suspend fun next(collectId: Int): Any?
}

fun <T> FlowService.createFlow(flowId: Any): Flow<T> = object : Flow<T> {
    @OptIn(InternalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<T>) {
        val collectId = create(flowId)
        @Suppress("UNCHECKED_CAST")
        while (true) collector.emit(next(collectId) as T ?: return)
    }
}

typealias FlowFactory = (flowId: Any) -> Flow<*>

fun flowService(flowFactory: FlowFactory): FlowService {
    val nextCollectId = AtomicInteger(0)
    val collectId2channel = ThreadSafeMap<Int, Channel<Any?>>(16)
    return object : FlowService {
        override suspend fun create(flowId: Any): Int {
            val flow = flowFactory(flowId)
            val collectId = nextCollectId.incrementAndGet()
            val channel = Channel<Any?>()
            collectId2channel.put(collectId, channel)
            try {
                CoroutineScope(coroutineContext).launch {
                    tryFinally({
                        flow.collect { value -> channel.send(value) }
                        channel.send(null)
                    }) {
                        collectId2channel.remove(collectId)
                    }
                }
            } catch (e: Exception) {
                throw e.addSuppressed { collectId2channel.remove(collectId) }
            }
            return collectId
        }

        override suspend fun next(collectId: Int) = collectId2channel.get(collectId)!!.receive()
    }
}

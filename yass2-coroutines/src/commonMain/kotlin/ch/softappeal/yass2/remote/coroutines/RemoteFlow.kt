package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

/** The used serializer must be able to serialize [Int], the involved flowId and optional [Flow] types. */
public interface FlowService {
    public suspend fun create(flowId: Any): Int
    public suspend fun next(collectId: Int): Any?
    public suspend fun cancel(collectId: Int)
}

@OptIn(FlowPreview::class)
public fun <T> FlowService.createFlow(flowId: Any): Flow<T> = object : AbstractFlow<T>() {
    override suspend fun collectSafely(collector: FlowCollector<T>) {
        val collectId = create(flowId)
        try {
            while (true) {
                val value = next(collectId) ?: return
                @Suppress("UNCHECKED_CAST")
                collector.emit(value as T)
            }
        } catch (e: Exception) {
            throw e.addSuppressed { cancel(collectId) }
        }
    }
}

public typealias FlowFactory = (flowId: Any) -> Flow<*>

public fun flowService(flowFactory: FlowFactory): FlowService {
    val nextCollectId = AtomicInteger(0)
    val collectId2channel = ThreadSafeMap<Int, Channel<Reply?>>(16)
    return object : FlowService {
        override suspend fun create(flowId: Any): Int {
            val flow = flowFactory(flowId)
            val collectId = nextCollectId.incrementAndGet()
            val channel = Channel<Reply?>()
            collectId2channel.put(collectId, channel)
            try {
                CoroutineScope(coroutineContext).launch {
                    tryFinally({
                        try {
                            flow.collect { value -> channel.send(ValueReply(value)) }
                            channel.send(null)
                        } catch (e: Exception) {
                            channel.send(ExceptionReply(e))
                        }
                    }) {
                        collectId2channel.remove(collectId)
                    }
                }
            } catch (e: Exception) {
                throw e.addSuppressed { collectId2channel.remove(collectId) }
            }
            return collectId
        }

        override suspend fun next(collectId: Int): Any? {
            val reply = collectId2channel.get(collectId)!!.receive()
            return reply?.process()
        }

        override suspend fun cancel(collectId: Int) {
            val channel = collectId2channel.remove(collectId)
            channel?.cancel()
        }
    }
}

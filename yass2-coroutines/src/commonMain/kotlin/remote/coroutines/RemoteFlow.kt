package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.tryCatch
import ch.softappeal.yass2.tryFinally
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

@GenerateProxy
public interface FlowService<out F, I> {
    public suspend fun create(flowId: I): Int
    public suspend fun next(collectId: Int): F?
    public suspend fun cancel(collectId: Int)
}

public fun <F, I> FlowService<F, I>.createFlow(flowId: I): Flow<F> =
    @OptIn(ExperimentalCoroutinesApi::class) object : AbstractFlow<F>() {
        override suspend fun collectSafely(collector: FlowCollector<F>) {
            val collectId = create(flowId)
            tryCatch({
                while (true) {
                    val value = next(collectId) ?: return
                    collector.emit(value)
                }
            }) {
                cancel(collectId)
            }
        }
    }

public typealias FlowFactory<F, I> = (flowId: I) -> Flow<F>

public fun <F, I> flowService(flowFactory: FlowFactory<F, I>): FlowService<F, I> {
    val nextCollectId = AtomicInteger(0)
    val collectId2channel = ThreadSafeMap<Int, Channel<Reply?>>(16)
    return object : FlowService<F, I> {
        override suspend fun create(flowId: I): Int {
            val collectId = nextCollectId.incrementAndGet()
            val channel = Channel<Reply?>()
            collectId2channel.put(collectId, channel)
            tryCatch({
                val flow = flowFactory(flowId)
                CoroutineScope(coroutineContext).launch {
                    tryFinally({
                        try {
                            flow.collect { channel.send(ValueReply(it)) }
                            channel.send(null)
                        } catch (e: Exception) {
                            channel.send(ExceptionReply(e))
                        }
                    }) {
                        collectId2channel.remove(collectId)
                    }
                }
            }) {
                collectId2channel.remove(collectId)
            }
            return collectId
        }

        override suspend fun next(collectId: Int): F? {
            val reply = collectId2channel.get(collectId)!!.receive()
            @Suppress("UNCHECKED_CAST") return reply?.process() as F
        }

        override suspend fun cancel(collectId: Int) {
            val channel = collectId2channel.remove(collectId)
            channel?.cancel()
        }
    }
}

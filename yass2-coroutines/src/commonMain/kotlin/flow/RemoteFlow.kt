package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.addSuppressed
import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Reply
import ch.softappeal.yass2.core.remote.ValueReply
import ch.softappeal.yass2.core.tryFinally
import ch.softappeal.yass2.coroutines.ThreadSafeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.coroutines.coroutineContext

public interface FlowService<out F, I> {
    public suspend fun create(flowId: I): Int
    public suspend fun next(collectId: Int): F?
    public suspend fun cancel(collectId: Int)
}

@ExperimentalApi
public fun <F, I> FlowService<F, I>.createFlow(flowId: I): Flow<F> =
    @OptIn(ExperimentalCoroutinesApi::class) // TODO: might become binary incompatible with future versions
    object : AbstractFlow<F>() {
        override suspend fun collectSafely(collector: FlowCollector<F>) {
            val collectId = create(flowId)
            try {
                while (true) {
                    val value = next(collectId) ?: return
                    collector.emit(value)
                }
            } catch (e: Exception) {
                throw e.addSuppressed { cancel(collectId) }
            }
        }
    }

public typealias FlowFactory<F, I> = (flowId: I) -> Flow<F>

@ExperimentalApi
@OptIn(ExperimentalAtomicApi::class) // TODO: might become binary incompatible with future versions
public fun <F, I> flowService(flowFactory: FlowFactory<F, I>): FlowService<F, I> {
    val nextCollectId = AtomicInt(0)
    val collectId2channel = ThreadSafeMap<Int, Channel<Reply?>>(16)
    return object : FlowService<F, I> {
        override suspend fun create(flowId: I): Int {
            val flow = flowFactory(flowId)
            val collectId = nextCollectId.incrementAndFetch()
            val channel = Channel<Reply?>()
            collectId2channel.put(collectId, channel)
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

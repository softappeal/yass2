package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.addSuppressed
import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Reply
import ch.softappeal.yass2.core.remote.ValueReply
import ch.softappeal.yass2.core.tryFinally
import ch.softappeal.yass2.coroutines.AtomicInt
import ch.softappeal.yass2.coroutines.ThreadSafeMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
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
public fun <F, I> flowService(flowFactory: FlowFactory<F, I>): FlowService<F, I> {
    val nextCollectId = AtomicInt(0)
    val collectIdToChannel = ThreadSafeMap<Int, Channel<Reply?>>(16)
    return object : FlowService<F, I> {
        override suspend fun create(flowId: I): Int {
            val flow = flowFactory(flowId)
            val collectId = nextCollectId.incrementAndFetch()
            val channel = Channel<Reply?>()
            collectIdToChannel.put(collectId, channel)
            CoroutineScope(coroutineContext).launch {
                tryFinally({
                    try {
                        flow.collect { channel.send(ValueReply(it)) }
                        channel.send(null)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        channel.send(ExceptionReply(e))
                    }
                }) {
                    collectIdToChannel.remove(collectId)
                }
            }
            return collectId
        }

        override suspend fun next(collectId: Int): F? {
            val reply = collectIdToChannel.get(collectId)!!.receive()
            @Suppress("UNCHECKED_CAST") return reply?.process() as F
        }

        override suspend fun cancel(collectId: Int) {
            val channel = collectIdToChannel.remove(collectId)
            channel?.cancel()
        }
    }
}

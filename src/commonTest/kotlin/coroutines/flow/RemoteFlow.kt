package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.addSuppressed
import ch.softappeal.yass2.coroutines.AtomicInt
import ch.softappeal.yass2.coroutines.ThreadSafeMap
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.tryFinally
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

interface FlowService<out F, I> {
    suspend fun create(flowId: I): Int
    suspend fun next(collectId: Int): F?
    suspend fun cancel(collectId: Int)
}

fun <F, I> FlowService<F, I>.createFlow(flowId: I): Flow<F> =
    object : Flow<F> {
        override suspend fun collect(collector: FlowCollector<F>) {
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

fun <F, I> flowService(flowFactory: (flowId: I) -> Flow<F>): FlowService<F, I> {
    val nextCollectId = AtomicInt(0)
    val collectIdToChannel = ThreadSafeMap<Int, Channel<Reply?>>(16)
    return object : FlowService<F, I> {
        override suspend fun create(flowId: I): Int {
            val flow = flowFactory(flowId)
            val collectId = nextCollectId.incrementAndFetch()
            val channel = Channel<Reply?>()
            collectIdToChannel.put(collectId, channel)
            CoroutineScope(currentCoroutineContext()).launch {
                tryFinally({
                    try {
                        flow.collect { channel.send(ValueReply(it)) }
                        channel.send(null)
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

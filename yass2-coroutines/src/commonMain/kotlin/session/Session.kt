package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.remote.Message
import ch.softappeal.yass2.core.remote.Reply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.tryFinally
import ch.softappeal.yass2.coroutines.AtomicBoolean
import ch.softappeal.yass2.coroutines.AtomicInt
import ch.softappeal.yass2.coroutines.ThreadSafeMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

public class Packet(public val requestNumber: Int, public val message: Message)

@ExperimentalApi public interface Connection {
    public suspend fun write(packet: Packet?)
    public suspend fun closed()
}

@ExperimentalApi public abstract class Session<C : Connection> {
    public open fun opened() {}

    /** [e] is `null` for regular close. */
    protected open suspend fun closed(e: Exception?) {}

    /** Is idempotent. */
    public suspend fun close(): Unit = close(true, null)

    /** Is idempotent. */
    public suspend fun close(e: Exception): Unit = close(false, e)

    public suspend fun isClosed(): Boolean = closed.load()

    private suspend fun closeOnException(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            close(e)
        }
    }

    protected val clientTunnel: Tunnel = { request ->
        check(!isClosed()) { "session '$this' is closed" }
        val requestNumber = nextRequestNumber.incrementAndFetch()
        val deferred = CompletableDeferred<Reply>(currentCoroutineContext()[Job]!!)
        requestNumberToDeferred.put(requestNumber, deferred)
        try {
            write(Packet(requestNumber, request))
            deferred.await()
        } catch (e: Exception) {
            requestNumberToDeferred.remove(requestNumber)
            throw e
        }
    }

    protected open val serverTunnel: Tunnel = { throw UnsupportedOperationException() }

    private lateinit var _connection: C
    public var connection: C
        get() = _connection
        internal set(value) {
            _connection = value
        }

    private val closed = AtomicBoolean(false)
    private val nextRequestNumber = AtomicInt(0)
    private val requestNumberToDeferred = ThreadSafeMap<Int, CompletableDeferred<Reply>>(16)
    private val writeMutex = Mutex()

    private suspend fun write(packet: Packet?): Unit = writeMutex.withLock { connection.write(packet) }

    private suspend fun close(sendEnd: Boolean, e: Exception?) {
        if (closed.exchange(true)) return
        tryFinally({
            closed(e)
            if (sendEnd) {
                write(null)
                delay(1000) // give some time to send the packet before close
            }
        }) {
            connection.closed()
        }
    }

    private suspend fun received(packet: Packet?) {
        if (packet == null) {
            close(false, null)
            return
        }
        check(!isClosed()) { "session '$this' is closed" }
        when (val message = packet.message) {
            is Request -> write(Packet(packet.requestNumber, serverTunnel(message)))
            is Reply -> requestNumberToDeferred.remove(packet.requestNumber)!!.complete(message)
        }
    }

    internal suspend fun receiveLoop(receive: suspend () -> Packet?) = closeOnException {
        opened()
        do {
            val packet = receive()
            received(packet)
        } while (packet != null)
    }

    /** Launches a new coroutine that closes the session if [heartbeat] throws an exception or doesn't return within [timeoutMillis]. */
    public fun CoroutineScope.heartbeat(
        intervalMillis: Long,
        timeoutMillis: Long,
        heartbeat: suspend () -> Unit,
    ): Job {
        require(intervalMillis > 0)
        require(timeoutMillis > 0)
        return launch {
            closeOnException {
                while (true) {
                    withTimeout(timeoutMillis) { heartbeat() }
                    delay(intervalMillis)
                }
            }
        }
    }
}

@ExperimentalApi public typealias SessionFactory<C> = () -> Session<C>

@ExperimentalApi public suspend fun <C : Connection> C.receiveLoop(
    sessionFactory: SessionFactory<C>, receive: suspend () -> Packet?
) {
    sessionFactory()
        .apply { connection = this@receiveLoop }
        .receiveLoop(receive)
}

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
/** Documents a [ServiceId] that must be implemented by initiator. */
@ExperimentalApi public annotation class MustBeImplementedByInitiator

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
/** Documents a [ServiceId] that must be implemented by acceptor. */
@ExperimentalApi public annotation class MustBeImplementedByAcceptor

package ch.softappeal.yass2.coroutines

import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.remote.Message
import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.tryFinally
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

public class Packet(public val requestNumber: Int, public val message: Message)

public interface Connection {
    public suspend fun write(packet: Packet?)
    public suspend fun closed()
}

private class ThreadSafeMap<K, V>(initialCapacity: Int) {
    private val mutex = Mutex()
    private val map = HashMap<K, V>(initialCapacity)
    suspend fun put(key: K, value: V): Unit = mutex.withLock { map[key] = value }
    suspend fun remove(key: K): V? = mutex.withLock { map.remove(key) }
}

@OptIn(ExperimentalAtomicApi::class) // TODO: might become binary incompatible with the future versions of the standard library
public abstract class Session<C : Connection> {
    public open fun opened() {}

    /** [e] is `null` for regular close. */
    protected open suspend fun closed(e: Exception?) {}

    /** Is idempotent. */
    public suspend fun close(): Unit = close(true, null)

    /** Is idempotent. */
    public suspend fun close(e: Exception): Unit = close(false, e)

    public fun isClosed(): Boolean = closed.load()

    protected val clientTunnel: Tunnel = { request ->
        check(!isClosed()) { "session '$this' is closed" }
        suspendCancellableCoroutine { continuation ->
            CoroutineScope(continuation.context).launch {
                try {
                    val requestNumber = nextRequestNumber.incrementAndFetch()
                    continuation.invokeOnCancellation {
                        launch {
                            requestNumber2continuation.remove(requestNumber)
                            continuation.cancel()
                        }
                    }
                    requestNumber2continuation.put(requestNumber, continuation)
                    write(Packet(requestNumber, request))
                } catch (e: Exception) {
                    close(e)
                }
            }
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
    private val requestNumber2continuation = ThreadSafeMap<Int, Continuation<Reply>>(16)
    private val writeMutex = Mutex()

    private suspend fun write(packet: Packet?): Unit = writeMutex.withLock { connection.write(packet) }

    private suspend fun close(sendEnd: Boolean, e: Exception?) {
        if (closed.exchange(true)) return
        tryFinally({
            closed(e)
            if (sendEnd) write(null)
        }) {
            connection.closed()
        }
    }

    internal suspend fun received(packet: Packet?) {
        if (packet == null) {
            close(false, null)
            return
        }
        check(!isClosed()) { "session '$this' is closed" }
        when (val message = packet.message) {
            is Request -> write(Packet(packet.requestNumber, serverTunnel(message)))
            is Reply -> requestNumber2continuation.remove(packet.requestNumber)!!.resume(message)
        }
    }
}

@InternalApi
public fun connect(session1: Session<Connection>, session2: Session<Connection>) {
    class LocalConnection(val session: Session<Connection>) : Connection {
        override suspend fun write(packet: Packet?) = session.received(packet)
        override suspend fun closed() = session.close()
    }
    session1.connection = LocalConnection(session2)
    session2.connection = LocalConnection(session1)
    session1.opened()
    session2.opened()
}

public typealias SessionFactory<C> = () -> Session<C>

public suspend fun <C : Connection> C.receiveLoop(sessionFactory: SessionFactory<C>, receive: suspend () -> Packet?) {
    val session = sessionFactory().apply {
        connection = this@receiveLoop
    }
    try {
        session.opened()
        do {
            val packet = receive()
            session.received(packet)
        } while (packet != null)
    } catch (e: Exception) {
        session.close(e)
    }
}

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
/** Documents a [ServiceId] that must be implemented by initiator. */
public annotation class MustBeImplementedByInitiator

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
/** Documents a [ServiceId] that must be implemented by acceptor. */
public annotation class MustBeImplementedByAcceptor

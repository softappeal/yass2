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
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

public class Packet(public val requestNumber: Int, public val message: Message)

public interface Connection {
    public suspend fun write(packet: Packet?)
    public suspend fun closed()
}

public abstract class Session<C : Connection> {
    public open fun opened() {}

    /** [e] is `null` for regular close. */
    protected open suspend fun closed(e: Exception?) {}

    /** Is idempotent. */
    public suspend fun close(): Unit = close(true, null)

    /** Is idempotent. */
    public suspend fun close(e: Exception): Unit = close(false, e)

    public suspend fun isClosed(): Boolean = closed.get()

    protected val clientTunnel: Tunnel = { request ->
        suspendCancellableCoroutine { continuation ->
            CoroutineScope(continuation.context).launch {
                try {
                    val requestNumber = nextRequestNumber.incrementAndGet()
                    requestNumber2continuation.put(requestNumber, continuation)
                    continuation.invokeOnCancellation {
                        launch {
                            requestNumber2continuation.remove(requestNumber)
                        }
                    }
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
    private val nextRequestNumber = AtomicInteger(0)
    private val requestNumber2continuation = ThreadSafeMap<Int, Continuation<Reply>>(16)
    private val writeMutex = Mutex()

    private suspend fun write(packet: Packet?): Unit = writeMutex.withLock { connection.write(packet) }

    private suspend fun close(sendEnd: Boolean, e: Exception?) {
        if (closed.getAndSet(true)) return
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
        while (true) {
            val packet = receive()
            session.received(packet)
            if (packet == null) return
        }
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

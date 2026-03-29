package ch.softappeal.yass2.coroutines.session

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

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
    public suspend fun close() {
        close(true, null)
    }

    /** Is idempotent. */
    public suspend fun close(e: Exception) {
        close(false, e)
    }

    public suspend fun isClosed(): Boolean = closed.load()

    public suspend fun closeOnException(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            close(e)
        }
    }

    public val clientTunnel: Tunnel = { request ->
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

    public lateinit var connection: C
        internal set

    private val closed = AtomicBoolean(false)
    private val nextRequestNumber = AtomicInt(0)
    private val requestNumberToDeferred = ThreadSafeMap<Int, CompletableDeferred<Reply>>(16)
    private val writeMutex = Mutex()

    private suspend fun write(packet: Packet?) {
        writeMutex.withLock { connection.write(packet) }
    }

    private suspend fun close(sendEnd: Boolean, e: Exception?) {
        if (closed.exchange(true)) return
        tryFinally({
            closed(e)
            if (sendEnd) {
                write(null)
                delay(1000.milliseconds) // give some time to send the packet before close
            }
        }) {
            connection.closed()
        }
    }

    private suspend fun received(packet: Packet?) {
        check(!isClosed()) { "session '$this' is closed" }
        if (packet == null) {
            close(false, null)
            return
        }
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
}

public typealias SessionFactory<C> = () -> Session<C>

public suspend fun <C : Connection> C.receiveLoop(sessionFactory: SessionFactory<C>, receive: suspend () -> Packet?) {
    sessionFactory()
        .apply { connection = this@receiveLoop }
        .receiveLoop(receive)
}

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
/** Documents a [ServiceId] that must be implemented by initiator. */
public annotation class MustBeImplementedByInitiator

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
/** Documents a [ServiceId] that must be implemented by acceptor. */
public annotation class MustBeImplementedByAcceptor

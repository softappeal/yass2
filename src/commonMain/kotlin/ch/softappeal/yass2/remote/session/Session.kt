package ch.softappeal.yass2.remote.session

import ch.softappeal.yass2.remote.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*
import kotlin.coroutines.*

class Packet(val requestNumber: Int, val message: Message)

interface Connection {
    suspend fun write(packet: Packet?)
    suspend fun closed()
}

abstract class Session {
    protected open fun opened() {}
    internal fun internalOpened(): Unit = opened()

    /** [e] is `null` for regular close. */
    protected open suspend fun closed(e: Exception?) {}

    /** Is idempotent. */
    suspend fun close(): Unit = close(true, null)

    suspend fun isClosed(): Boolean = closed.get()

    protected val clientTunnel: Tunnel = { request ->
        suspendCancellableCoroutine { continuation ->
            CoroutineScope(continuation.context).launch {
                try {
                    val requestNumber = nextRequestNumber.incrementAndGet()
                    requestNumber2continuation.put(requestNumber, continuation)
                    write(Packet(requestNumber, request))
                } catch (e: Exception) {
                    close(e)
                }
            }
        }
    }

    protected open val serverTunnel: Tunnel = { throw UnsupportedOperationException() }

    lateinit var connection: Connection
        internal set

    private val closed = AtomicBoolean(false)
    private val nextRequestNumber = AtomicInteger(0)
    private val requestNumber2continuation = ThreadSafeMap<Int, Continuation<Reply>>(16)
    private val writeMutex = Mutex()

    private suspend fun write(packet: Packet?): Unit = writeMutex.withLock { connection.write(packet) }

    private suspend fun close(sendEnd: Boolean, e: Exception?) {
        if (closed.getAndSet(true)) return
        try {
            closed(e)
            if (sendEnd) write(null)
        } finally {
            connection.closed()
        }
    }

    internal suspend fun close(e: Exception): Unit = close(false, e)

    internal suspend fun received(packet: Packet?) {
        if (packet == null) {
            close(false, null)
            return
        }
        when (val message = packet.message) {
            is Request -> write(Packet(packet.requestNumber, serverTunnel(message)))
            is Reply -> requestNumber2continuation.remove(packet.requestNumber)!!.resume(message)
            else -> error("unexpected '$message'")
        }
    }
}

typealias SessionFactory = () -> Session

suspend fun Connection.receiveLoop(sessionFactory: SessionFactory, receive: suspend () -> Packet?) {
    val session = sessionFactory()
    session.connection = this
    try {
        session.internalOpened()
        while (true) {
            val packet = receive()
            session.received(packet)
            if (packet == null) return
        }
    } catch (e: Exception) {
        session.close(e)
    }
}

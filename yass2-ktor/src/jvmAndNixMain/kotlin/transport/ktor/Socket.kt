package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.remote.coroutines.session.Connection
import ch.softappeal.yass2.remote.coroutines.session.Packet
import ch.softappeal.yass2.remote.coroutines.session.SessionFactory
import ch.softappeal.yass2.remote.coroutines.session.receiveLoop
import ch.softappeal.yass2.transport.Transport
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.use
import io.ktor.utils.io.readInt
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private suspend fun ByteReadChannel.read(transport: Transport): Any? = read(transport, readInt())

public typealias SocketConnector = suspend () -> Socket

public fun Transport.socketTunnel(socketConnector: SocketConnector): Tunnel = { request ->
    socketConnector().use { socket ->
        val writeChannel = socket.openWriteChannel()
        writeChannel.write(this, request)
        writeChannel.flush()
        socket.openReadChannel().read(this) as Reply
    }
}

public class SocketCce(public val socket: Socket) : AbstractCoroutineContextElement(SocketCce) {
    public companion object Key : CoroutineContext.Key<SocketCce>
}

public suspend fun Socket.handleRequest(transport: Transport, tunnel: Tunnel): Unit = use {
    withContext(SocketCce(this)) {
        val reply = tunnel(openReadChannel().read(transport) as Request)
        val writeChannel = openWriteChannel()
        writeChannel.write(transport, reply)
        writeChannel.flush()
    }
}

public class SocketConnection internal constructor(
    private val transport: Transport,
    public val socket: Socket,
) : Connection {
    private val writeChannel = socket.openWriteChannel()
    override suspend fun write(packet: Packet?) {
        writeChannel.write(transport, packet)
        writeChannel.flush()
    }

    override suspend fun closed() {
        // the following line closes socket on jvm and native,
        // see https://youtrack.jetbrains.com/issue/KTOR-5093/Native-Read-from-a-closed-socket-doesnt-throw-an-exception
        socket.cancel()
    }
}

public suspend fun Socket.receiveLoop(transport: Transport, sessionFactory: SessionFactory<SocketConnection>): Unit = use {
    val readChannel = openReadChannel()
    SocketConnection(transport, this).receiveLoop(sessionFactory) { readChannel.read(transport) as Packet? }
}

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.serialize.readBytes
import ch.softappeal.yass2.session.Connection
import ch.softappeal.yass2.session.Packet
import ch.softappeal.yass2.session.SessionFactory
import ch.softappeal.yass2.session.receiveLoop
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readInt
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeInt
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private suspend fun ByteWriteChannel.write(transport: Transport, value: Any?) {
    @OptIn(InternalApi::class) val writer = transport.createWriter()
    transport.write(writer, value)
    writeInt(writer.current)
    writeFully(writer.buffer, 0, writer.current)
}

private suspend fun ByteReadChannel.read(transport: Transport): Any? {
    @OptIn(InternalApi::class) val buffer = transport.readBytes(readInt()) { bytes, offset, size ->
        readFully(bytes, offset, offset + size)
    }
    return transport.readBytes(buffer)
}

public typealias SocketConnector = suspend () -> Socket

public fun Transport.tunnel(socketConnector: SocketConnector): Tunnel = { request ->
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

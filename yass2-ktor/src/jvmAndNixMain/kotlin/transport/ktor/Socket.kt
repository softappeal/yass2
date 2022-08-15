package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.remote.coroutines.session.Connection
import ch.softappeal.yass2.transport.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

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

    override suspend fun closed(): Unit = socket.close()
}

public suspend fun Socket.receiveLoop(transport: Transport, sessionFactory: SessionFactory): Unit = use {
    val readChannel = openReadChannel()
    SocketConnection(transport, this).receiveLoop(sessionFactory) { readChannel.read(transport) as Packet? }
}

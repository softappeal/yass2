package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.remote.coroutines.session.Connection
import ch.softappeal.yass2.transport.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

private suspend fun ByteReadChannel.read(config: TransportConfig): Any? = read(config, readInt())

public typealias SocketConnector = suspend () -> Socket

public fun TransportConfig.socketTunnel(socketConnector: SocketConnector): Tunnel = { request ->
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

public suspend fun Socket.handleRequest(config: TransportConfig, tunnel: Tunnel): Unit = use {
    withContext(SocketCce(this)) {
        val reply = tunnel(openReadChannel().read(config) as Request)
        val writeChannel = openWriteChannel()
        writeChannel.write(config, reply)
        writeChannel.flush()
    }
}

public class SocketConnection internal constructor(
    private val config: TransportConfig,
    public val socket: Socket,
) : Connection {
    private val writeChannel = socket.openWriteChannel()
    override suspend fun write(packet: Packet?) {
        writeChannel.write(config, packet)
        writeChannel.flush()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun closed(): Unit = socket.close()
}

public suspend fun Socket.receiveLoop(config: TransportConfig, sessionFactory: SessionFactory): Unit = use {
    val readChannel = openReadChannel()
    SocketConnection(config, this).receiveLoop(sessionFactory) { readChannel.read(config) as Packet? }
}

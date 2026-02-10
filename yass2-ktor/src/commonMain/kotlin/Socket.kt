package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.remote.Reply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.serialize.ByteArrayWriter
import ch.softappeal.yass2.core.serialize.Serializer
import ch.softappeal.yass2.core.serialize.fromByteArray
import ch.softappeal.yass2.coroutines.session.Connection
import ch.softappeal.yass2.coroutines.session.Packet
import ch.softappeal.yass2.coroutines.session.SessionFactory
import ch.softappeal.yass2.coroutines.session.receiveLoop
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

private suspend fun ByteWriteChannel.write(serializer: Serializer, value: Any?) {
    val writer = ByteArrayWriter()
    serializer.write(writer, value)
    val byteArray = writer.toyByteArray()
    writeInt(byteArray.size)
    writeFully(byteArray)
}

internal suspend fun readByteArray(
    length: Int,
    readByteArray: suspend (byteArray: ByteArray, offset: Int, length: Int) -> Unit,
): ByteArray {
    var byteArray = ByteArray(minOf(length, 1000))
    var current = 0
    while (current < length) { // prevents easy out-of-memory attack
        if (current >= byteArray.size) byteArray = byteArray.copyOf(minOf(length, 2 * byteArray.size))
        readByteArray(byteArray, current, byteArray.size - current)
        current = byteArray.size
    }
    return byteArray
}

private suspend fun ByteReadChannel.read(serializer: Serializer): Any? {
    val byteArray = readByteArray(readInt()) { byteArray, offset, length ->
        readFully(byteArray, offset, offset + length)
    }
    return serializer.fromByteArray(byteArray)
}

@ExperimentalApi public typealias SocketConnector = suspend () -> Socket

@ExperimentalApi public fun Serializer.tunnel(socketConnector: SocketConnector): Tunnel = { request ->
    socketConnector().use { socket ->
        val writeChannel = socket.openWriteChannel()
        writeChannel.write(this, request)
        writeChannel.flush()
        socket.openReadChannel().read(this) as Reply
    }
}

@ExperimentalApi public class SocketCce(public val socket: Socket) : AbstractCoroutineContextElement(SocketCce) {
    public companion object Key : CoroutineContext.Key<SocketCce>
}

@ExperimentalApi public suspend fun Socket.handleRequest(serializer: Serializer, tunnel: Tunnel): Unit = use {
    withContext(SocketCce(this)) {
        val reply = tunnel(openReadChannel().read(serializer) as Request)
        val writeChannel = openWriteChannel()
        writeChannel.write(serializer, reply)
        writeChannel.flush()
    }
}

@ExperimentalApi public class SocketConnection internal constructor(
    private val serializer: Serializer,
    public val socket: Socket,
) : Connection {
    private val writeChannel = socket.openWriteChannel()
    override suspend fun write(packet: Packet?) {
        writeChannel.write(serializer, packet)
        writeChannel.flush()
    }

    override suspend fun closed() {
        socket.cancel() // closes socket, see https://youtrack.jetbrains.com/issue/KTOR-5093/Native-Read-from-a-closed-socket-doesnt-throw-an-exception
    }
}

@ExperimentalApi public suspend fun Socket.receiveLoop(
    serializer: Serializer, sessionFactory: SessionFactory<SocketConnection>
): Unit = use {
    val readChannel = openReadChannel()
    SocketConnection(serializer, this).receiveLoop(sessionFactory) { readChannel.read(serializer) as Packet? }
}

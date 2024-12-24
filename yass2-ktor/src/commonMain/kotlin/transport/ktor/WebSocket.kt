package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.coroutines.session.Connection
import ch.softappeal.yass2.remote.coroutines.session.Packet
import ch.softappeal.yass2.remote.coroutines.session.SessionFactory
import ch.softappeal.yass2.remote.coroutines.session.receiveLoop
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.transport.BytesReader
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close

private class BytesWriter(var buffer: ByteArray) : Writer {
    constructor(initialCapacity: Int) : this(ByteArray(initialCapacity))

    var current: Int = 0

    override fun writeByte(byte: Byte) {
        if (current >= buffer.size) buffer = buffer.copyOf(maxOf(1000, 2 * buffer.size))
        buffer[current++] = byte
    }

    override fun writeBytes(bytes: ByteArray) {
        val newCurrent = current + bytes.size
        if (newCurrent > buffer.size) buffer = buffer.copyOf(maxOf(newCurrent + 1000, 2 * buffer.size))
        bytes.copyInto(buffer, current)
        current = newCurrent
    }
}

public class WebSocketConnection internal constructor(
    private val packetSerializer: Serializer,
    public val session: WebSocketSession,
) : Connection {
    override suspend fun write(packet: Packet?) {
        val writer = BytesWriter(1000)
        packetSerializer.write(writer, packet)
        session.outgoing.send(Frame.Binary(true, writer.buffer.copyOfRange(0, writer.current)))
    }

    override suspend fun closed(): Unit = session.close()
}

public suspend fun WebSocketSession.receiveLoop(packetSerializer: Serializer, sessionFactory: SessionFactory<WebSocketConnection>) {
    WebSocketConnection(packetSerializer, this).receiveLoop(sessionFactory) {
        val reader = BytesReader((incoming.receive() as Frame.Binary).data)
        val packet = packetSerializer.read(reader) as Packet?
        check(reader.isDrained)
        packet
    }
}

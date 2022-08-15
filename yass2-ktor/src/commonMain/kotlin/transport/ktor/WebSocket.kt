package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.transport.*
import io.ktor.websocket.*

public class WebSocketConnection internal constructor(
    private val transport: Transport,
    public val session: WebSocketSession,
) : Connection {
    override suspend fun write(packet: Packet?) {
        val writer = transport.writer()
        transport.write(writer, packet)
        session.outgoing.send(Frame.Binary(true, writer.buffer.copyOfRange(0, writer.current)))
    }

    override suspend fun closed(): Unit = session.close()
}

public suspend fun WebSocketSession.receiveLoop(transport: Transport, sessionFactory: SessionFactory) {
    WebSocketConnection(transport, this).receiveLoop(sessionFactory) {
        val reader = BytesReader((incoming.receive() as Frame.Binary).data)
        val packet = transport.read(reader) as Packet?
        check(reader.isDrained)
        packet
    }
}

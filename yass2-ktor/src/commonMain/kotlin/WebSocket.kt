package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.coroutines.Connection
import ch.softappeal.yass2.coroutines.Packet
import ch.softappeal.yass2.coroutines.SessionFactory
import ch.softappeal.yass2.coroutines.receiveLoop
import ch.softappeal.yass2.serialize.readBytes
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close

public class WebSocketConnection internal constructor(
    private val transport: Transport,
    public val session: WebSocketSession,
) : Connection {
    override suspend fun write(packet: Packet?) {
        @OptIn(InternalApi::class) val writer = transport.createWriter()
        transport.write(writer, packet)
        session.outgoing.send(Frame.Binary(true, writer.buffer.copyOf(writer.current)))
    }

    override suspend fun closed(): Unit = session.close()
}

public suspend fun WebSocketSession.receiveLoop(transport: Transport, sessionFactory: SessionFactory<WebSocketConnection>) {
    WebSocketConnection(transport, this).receiveLoop(sessionFactory) {
        val buffer = (incoming.receive() as Frame.Binary).data
        transport.readBytes(buffer) as Packet?
    }
}

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.transport.*
import io.ktor.http.cio.websocket.*

public class WebSocketConnection internal constructor(
    private val config: TransportConfig,
    public val session: WebSocketSession,
) : Connection {
    override suspend fun write(packet: Packet?) {
        val writer = config.writer()
        config.write(writer, packet)
        session.outgoing.send(Frame.Binary(true, writer.buffer.copyOfRange(0, writer.current)))
    }

    override suspend fun closed(): Unit = session.close()
}

public suspend fun WebSocketSession.receiveLoop(config: TransportConfig, sessionFactory: SessionFactory) {
    WebSocketConnection(config, this).receiveLoop(sessionFactory) {
        val reader = BytesReader((incoming.receive() as Frame.Binary).data)
        val packet = config.read(reader) as Packet?
        check(reader.drained)
        packet
    }
}

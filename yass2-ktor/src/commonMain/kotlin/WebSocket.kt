package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.core.serialize.Serializer
import ch.softappeal.yass2.core.serialize.fromByteArray
import ch.softappeal.yass2.core.serialize.toByteArray
import ch.softappeal.yass2.coroutines.session.Connection
import ch.softappeal.yass2.coroutines.session.Packet
import ch.softappeal.yass2.coroutines.session.SessionFactory
import ch.softappeal.yass2.coroutines.session.receiveLoop
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close

public class WebSocketConnection internal constructor(
    private val serializer: Serializer, public val session: WebSocketSession,
) : Connection {
    override suspend fun write(packet: Packet?) {
        val byteArray = serializer.toByteArray(packet)
        session.outgoing.send(Frame.Binary(true, byteArray))
    }

    override suspend fun closed(): Unit = session.close()
}

public suspend fun WebSocketSession.receiveLoop(serializer: Serializer, sessionFactory: SessionFactory) {
    WebSocketConnection(serializer, this).receiveLoop(sessionFactory) {
        val byteArray = (incoming.receive() as Frame.Binary).data
        serializer.fromByteArray(byteArray) as Packet?
    }
}

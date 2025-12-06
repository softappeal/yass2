package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.coroutines.session.Connection
import ch.softappeal.yass2.coroutines.session.Packet
import ch.softappeal.yass2.coroutines.session.SessionFactory
import ch.softappeal.yass2.coroutines.session.receiveLoop
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.fromByteArray
import ch.softappeal.yass2.serialize.toByteArray
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close

class WebSocketConnection internal constructor(
    private val serializer: Serializer, val session: WebSocketSession,
) : Connection {
    override suspend fun write(packet: Packet?) {
        val byteArray = serializer.toByteArray(packet)
        session.outgoing.send(Frame.Binary(true, byteArray))
    }

    override suspend fun closed(): Unit = session.close()
}

suspend fun WebSocketSession.receiveLoop(serializer: Serializer, sessionFactory: SessionFactory<WebSocketConnection>) {
    WebSocketConnection(serializer, this).receiveLoop(sessionFactory) {
        val byteArray = (incoming.receive() as Frame.Binary).data
        serializer.fromByteArray(byteArray) as Packet?
    }
}

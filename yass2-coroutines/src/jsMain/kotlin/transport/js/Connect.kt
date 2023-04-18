package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.transport.*
import kotlinx.coroutines.*
import org.khronos.webgl.*
import org.w3c.dom.*

public fun Transport.connect(url: String, sessionFactory: SessionFactory<Connection>) {
    WebSocket(url).apply {
        binaryType = BinaryType.ARRAYBUFFER
        onopen = {
            val session = object : Connection {
                override suspend fun write(packet: Packet?) {
                    val writer = createWriter()
                    write(writer, packet)
                    val data = writer.buffer.asDynamic().subarray(0, writer.current)
                    send(@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE") (data as ArrayBufferView))
                }

                override suspend fun closed() = close()
            }.createSession(sessionFactory)
            onmessage = { event ->
                @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
                    try {
                        val buffer = event.data as ArrayBuffer
                        val reader = BytesReader(Int8Array(buffer).asDynamic() as ByteArray)
                        val packet = read(reader) as Packet?
                        check(reader.isDrained)
                        session.implReceived(packet)
                    } catch (e: Exception) {
                        session.close(e)
                    }
                }
            }
            fun close(e: Exception) {
                @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch { session.close(e) }
            }
            onclose = { close(Exception("onclose")) }
            onerror = { close(Exception("onerror")) }
            try {
                session.opened()
            } catch (e: Exception) {
                close(e)
            }
        }
    }
}

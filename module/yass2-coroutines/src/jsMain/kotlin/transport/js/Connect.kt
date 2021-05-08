package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.transport.*
import kotlinx.coroutines.*
import org.khronos.webgl.*
import org.w3c.dom.*

public fun TransportConfig.connect(url: String, sessionFactory: SessionFactory) {
    WebSocket(url).apply {
        binaryType = BinaryType.ARRAYBUFFER
        onopen = {
            val session = sessionFactory()
            session.connection = object : Connection {
                override suspend fun write(packet: Packet?) {
                    val writer = writer()
                    write(writer, packet)
                    val data = (writer.buffer.asDynamic()).subarray(0, writer.current)
                    send(@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE") (data as ArrayBufferView))
                }

                override suspend fun closed() = close()
            }
            onmessage = { event ->
                @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch {
                    try {
                        val buffer = event.data as ArrayBuffer
                        val reader = BytesReader(Int8Array(buffer).asDynamic() as ByteArray)
                        val packet = read(reader) as Packet?
                        check(reader.drained)
                        session.received(packet)
                    } catch (e: Exception) {
                        session.close(e)
                    }
                }
            }
            fun close(reason: String) {
                @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch { session.close(Exception(reason)) }
            }
            onclose = { close("onclose") }
            onerror = { close("onerror") }
            session.opened()
        }
    }
}

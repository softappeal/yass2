package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.transport.BytesReader
import ch.softappeal.yass2.transport.Transport
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.w3c.fetch.RequestInit

public fun Transport.tunnel(url: String): Tunnel = { request ->
    val writer = createWriter()
    write(writer, request)
    val response = window.fetch(url, object : RequestInit {
        override var method: String? = "POST"
        override var body = writer.buffer.asDynamic().subarray(0, writer.current)
    }).await()
    val buffer = response.arrayBuffer().await()
    val reader = BytesReader(Int8Array(buffer).asDynamic() as ByteArray)
    val reply = read(reader) as Reply
    check(reader.isDrained)
    reply
}

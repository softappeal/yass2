package ch.softappeal.yass2.transport.js

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.*
import kotlinx.browser.*
import kotlinx.coroutines.*
import org.khronos.webgl.*
import org.w3c.fetch.*

public fun Transport.tunnel(url: String): Tunnel = { request ->
    val writer = createWriter()
    write(writer, request)
    val response = window.fetch(url, object : RequestInit {
        override var method: String? = "POST"
        override var body = (writer.buffer.asDynamic()).subarray(0, writer.current)
    }).await()
    val buffer = response.arrayBuffer().await()
    val reader = BytesReader(Int8Array(buffer).asDynamic() as ByteArray)
    val reply = read(reader) as Reply
    check(reader.isDrained)
    reply
}

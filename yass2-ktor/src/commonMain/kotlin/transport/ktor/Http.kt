package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*

internal fun Transport.write(message: Message): OutgoingContent.WriteChannelContent {
    val writer = writer()
    write(writer, message)
    return object : OutgoingContent.WriteChannelContent() {
        override suspend fun writeTo(channel: ByteWriteChannel) = channel.writeFully(writer.buffer, 0, writer.current)
        override val contentLength get() = writer.current.toLong()
    }
}

public fun HttpClient.tunnel(
    transport: Transport,
    url: String,
    headers: () -> Headers = { Headers.Empty },
): Tunnel = { request ->
    val response = request(url) {
        method = HttpMethod.Post
        this.headers.appendAll(headers())
        setBody(transport.write(request))
    }
    val length = response.contentLength()!!.toInt()
    response.bodyAsChannel().read(transport, length) as Reply
}

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.Message
import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.transport.Transport
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteWriteChannel

internal fun Transport.write(message: Message): OutgoingContent.WriteChannelContent {
    val writer = createWriter()
    write(writer, message)
    return object : OutgoingContent.WriteChannelContent() {
        override suspend fun writeTo(channel: ByteWriteChannel) = channel.writeFully(writer)
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

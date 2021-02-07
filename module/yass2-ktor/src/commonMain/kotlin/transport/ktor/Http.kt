package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*

internal fun TransportConfig.write(message: Message): OutgoingContent.WriteChannelContent {
    val writer = writer()
    write(writer, message)
    return object : OutgoingContent.WriteChannelContent() {
        override suspend fun writeTo(channel: ByteWriteChannel) = channel.writeFully(writer.buffer, 0, writer.current)
        override val contentLength get() = writer.current.toLong()
    }
}

public fun HttpClient.tunnel(
    config: TransportConfig,
    url: String,
    headers: () -> Headers = { Headers.Empty },
): Tunnel = { request ->
    val response = request<HttpResponse>(url) {
        method = HttpMethod.Post
        this.headers.appendAll(headers())
        body = config.write(request)
    }
    val length = response.contentLength()!!.toInt()
    response.content.read(config, length) as Reply
}

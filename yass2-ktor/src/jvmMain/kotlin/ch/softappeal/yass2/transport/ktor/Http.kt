package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

private suspend fun ByteReadChannel.read(config: TransportConfig, length: Int): Any? {
    val buffer = config.readBytes(length) { bytes, offset, size -> readFully(bytes, offset, size) }
    val reader = BytesReader(buffer)
    val value = config.read(reader)
    check(reader.drained)
    return value
}

private fun TransportConfig.write(message: Message): OutgoingContent.WriteChannelContent {
    val writer = writer()
    write(writer, message)
    return object : OutgoingContent.WriteChannelContent() {
        override suspend fun writeTo(channel: ByteWriteChannel) = channel.writeFully(writer.buffer, 0, writer.current)
        override val contentLength get() = writer.current.toLong()
    }
}

public fun HttpClient.tunnel(config: TransportConfig, url: String, headers: Headers = headersOf()): Tunnel = { request ->
    val response = request<HttpResponse>(url) {
        method = HttpMethod.Post
        this.headers.appendAll(headers)
        body = config.write(request)
    }
    val length = response.contentLength()!!.toInt()
    response.content.read(config, length) as Reply
}

public class CallCce(public val call: ApplicationCall) : AbstractCoroutineContextElement(CallCce) {
    public companion object Key : CoroutineContext.Key<CallCce>
}

public fun Route.route(config: TransportConfig, path: String, tunnel: Tunnel) {
    route(path) {
        post {
            withContext(CallCce(call)) {
                val length = call.request.headers["Content-Length"]!!.toInt()
                val reply = tunnel(call.receiveChannel().read(config, length) as Request)
                call.respond(config.write(reply))
            }
        }
    }
}

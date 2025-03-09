package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.remote.Message
import ch.softappeal.yass2.remote.Reply
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Tunnel
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentLength
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private fun Transport.write(message: Message): OutgoingContent.WriteChannelContent {
    @OptIn(InternalApi::class) val writer = createWriter()
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

public class CallCce(public val call: ApplicationCall) : AbstractCoroutineContextElement(CallCce) {
    public companion object Key : CoroutineContext.Key<CallCce>
}

public fun Route.route(transport: Transport, path: String, tunnel: Tunnel) {
    route(path) {
        post {
            withContext(CallCce(call)) {
                val length = call.request.headers["Content-Length"]!!.toInt()
                val reply = tunnel(call.receiveChannel().read(transport, length) as Request)
                call.respond(transport.write(reply))
            }
        }
    }
}

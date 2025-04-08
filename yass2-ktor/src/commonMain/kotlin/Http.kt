package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.remote.Message
import ch.softappeal.yass2.core.remote.Reply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.serialize.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.Headers
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private fun Transport.write(message: Message): ByteArray {
    @OptIn(InternalApi::class) val writer = createWriter()
    write(writer, message)
    return writer.toyByteArray()
}

public fun HttpClient.tunnel(
    transport: Transport,
    url: String,
    headers: () -> Headers = { Headers.Empty },
): Tunnel = { request ->
    val response = post(url) {
        this.headers.appendAll(headers())
        setBody(transport.write(request))
    }
    transport.readBytes(response.bodyAsBytes()) as Reply
}

public class CallCce(public val call: ApplicationCall) : AbstractCoroutineContextElement(CallCce) {
    public companion object Key : CoroutineContext.Key<CallCce>
}

public fun Route.route(transport: Transport, path: String, tunnel: Tunnel) {
    route(path) {
        post {
            withContext(CallCce(call)) {
                val reply = tunnel(transport.readBytes(call.receive(ByteArray::class)) as Request)
                call.respond(transport.write(reply))
            }
        }
    }
}

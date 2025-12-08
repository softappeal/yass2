package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.remote.Reply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.serialize.Serializer
import ch.softappeal.yass2.core.serialize.fromByteArray
import ch.softappeal.yass2.core.serialize.toByteArray
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

@ExperimentalApi public fun HttpClient.tunnel(
    serializer: Serializer, url: String, headers: () -> Headers = { Headers.Empty }
): Tunnel = { request ->
    val response = post(url) {
        this.headers.appendAll(headers())
        setBody(serializer.toByteArray(request))
    }
    serializer.fromByteArray(response.bodyAsBytes()) as Reply
}

@ExperimentalApi public class CallCce(public val call: ApplicationCall) : AbstractCoroutineContextElement(CallCce) {
    public companion object Key : CoroutineContext.Key<CallCce>
}

@ExperimentalApi public fun Route.route(serializer: Serializer, path: String, tunnel: Tunnel) {
    route(path) {
        post {
            withContext(CallCce(call)) {
                val reply = tunnel(serializer.fromByteArray(call.receive(ByteArray::class)) as Request)
                call.respond(serializer.toByteArray(reply))
            }
        }
    }
}

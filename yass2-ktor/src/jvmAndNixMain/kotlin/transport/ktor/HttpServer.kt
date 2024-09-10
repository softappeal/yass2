package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.transport.Transport
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

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

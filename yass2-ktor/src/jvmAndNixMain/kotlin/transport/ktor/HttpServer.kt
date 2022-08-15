package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

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

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

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

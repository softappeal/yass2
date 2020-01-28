package ch.softappeal.yass2.transport.ktor.js

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.session.*
import ch.softappeal.yass2.transport.ktor.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlin.coroutines.*

@KtorExperimentalAPI // TODO
fun main() {
    println("http://$Host:$Port/index.html")
    suspend fun serverContext() = "http-" + coroutineContext[CallCce]?.call?.request?.uri!!
    fun Session.acceptorContext() = "ws-" + ((connection as WebSocketConnection).session as WebSocketServerSession).call.request.uri
    embeddedServer(io.ktor.server.cio.CIO, Port) {
        install(WebSockets)
        routing {
            static {
                files("./")
                files("./src/jsTest/resources")
                files("./build/js/node_modules")
            }
            route(Config, Path, tunnel(::serverContext))
            webSocket(Path) {
                receiveLoop(Config, acceptorSessionFactory { acceptorContext() })
            }
        }
    }.start(wait = true)
}

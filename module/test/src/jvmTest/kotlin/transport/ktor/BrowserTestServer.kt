@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.coroutines.session.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

fun main() {
    println("http://$Host:$Port/index.html")
    embeddedServer(CIO, Port) {
        install(WebSockets)
        routing {
            static {
                files("./") // needed for debugging (sources)
                files("./module/test/src/jsTest/resources")
                files("./build/js/node_modules")
            }
            route(
                MessageTransport,
                Path,
                tunnel { "http-" + currentCoroutineContext()[CallCce]?.call?.request?.uri!! }
            )
            webSocket(Path) {
                receiveLoop(
                    PacketTransport,
                    acceptorSessionFactory { "ws-" + ((connection as WebSocketConnection).session as WebSocketServerSession).call.request.uri }
                )
            }
        }
    }.start(wait = true)
}

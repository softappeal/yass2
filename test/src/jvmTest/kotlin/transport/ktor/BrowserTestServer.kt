@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.coroutines.session.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import java.io.*

private fun Application.theModule() {
    install(WebSockets)
    routing {
        staticFiles("/", File("./")) // needed for debugging (sources)
        staticFiles("/", File("./build/js/packages/test-test/kotlin"))
        route(
            MessageTransport,
            PATH,
            tunnel { "http-" + currentCoroutineContext()[CallCce]?.call?.request?.uri!! }
        )
        webSocket(PATH) {
            receiveLoop(
                PacketTransport,
                acceptorSessionFactory { "ws-" + (connection.session as WebSocketServerSession).call.request.uri }
            )
        }
    }
}

fun main() {
    val port = 28947
    println("http://$LOCAL_HOST:$port")
    embeddedServer(CIO, port, module = Application::theModule).start(wait = true)
}

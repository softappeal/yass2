@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.contract.MessageTransport
import ch.softappeal.yass2.contract.PacketTransport
import ch.softappeal.yass2.remote.coroutines.session.acceptorSessionFactory
import ch.softappeal.yass2.remote.coroutines.session.tunnel
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.request.uri
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.currentCoroutineContext
import java.io.File

private fun Application.theModule() {
    install(WebSockets)
    routing {
        staticFiles("/", File("./build/js/packages/yass2-test-wasm-js-test/kotlin"))
        staticFiles("/", File("./")) // needed for debugging (sources)
        staticFiles("/", File("./test/")) // needed for debugging (sources)
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

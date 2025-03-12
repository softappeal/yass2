@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.coroutines.acceptorSessionFactory
import ch.softappeal.yass2.coroutines.tunnel
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
        // wasm
        staticFiles("/", File("./build/js/packages/test-wasm-test/kotlin"))
        staticFiles("/", File("./test/")) // needed for debugging (sources)

        // js
        staticFiles("/", File("./build/js/packages/test-test/kotlin"))

        // both
        staticFiles("/", File("./")) // needed for debugging (sources)
        route(
            ContractTransport,
            PATH,
            tunnel { "http-" + currentCoroutineContext()[CallCce]?.call?.request?.uri!! }
        )
        webSocket(PATH) {
            receiveLoop(
                ContractTransport,
                acceptorSessionFactory { "ws-" + (connection.session as WebSocketServerSession).call.request.uri }
            )
        }
    }
}

fun main() {
    println("http://$LOCAL_HOST:$PORT/index-js.html")
    println("http://$LOCAL_HOST:$PORT/index-wasm.html")
    embeddedServer(CIO, PORT, module = Application::theModule).start(wait = true)
}

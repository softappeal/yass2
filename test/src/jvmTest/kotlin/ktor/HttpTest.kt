package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.remote.tunnel
import ch.softappeal.yass2.session.acceptorSessionFactory
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test

val Server = embeddedServer(io.ktor.server.cio.CIO, PORT) {
    install(WebSockets)
    routing {
        route(
            ContractTransport,
            PATH,
            tunnel {
                currentCoroutineContext()[CallCce]!!.call.request.headers[DEMO_HEADER_KEY] ?: "no-header"
            }
        )
        webSocket(PATH) {
            receiveLoop(
                ContractTransport,
                acceptorSessionFactory {
                    (connection.session as WebSocketServerSession).call.request.headers[DEMO_HEADER_KEY] ?: "no-header"
                }
            )
        }
        staticFiles("/js", File("./build/js/packages/yass2-test-test/kotlin"))
        staticFiles("/wasm", File("./build/js/packages/yass2-test-wasm-js-test/kotlin"))
        staticFiles("/test", File("./test"))                         // sources for js
        staticFiles("/src", File("./test/src"))                      // sources for wasm
        staticFiles("/yass2-core", File("./yass2-core"))             // sources for ja and wasm
        staticFiles("/yass2-coroutines", File("./yass2-coroutines")) // sources for ja and wasm
        staticFiles("/yass2-ktor", File("./yass2-ktor"))             // sources for ja and wasm
    }
}

class HttpTest {
    @Test
    fun test() {
        Server.start()
        try {
            runBlocking {
                clientTest(io.ktor.client.engine.cio.CIO)
            }
        } finally {
            Server.stop()
        }
    }
}

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.coroutines.session.acceptorSessionFactory
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
        // code
        staticFiles("/js", File("./build/js/packages/yass2-yass2-ktor-test/kotlin"))
        staticFiles("/wasm", File("./build/js/packages/yass2-yass2-ktor-wasm-js-test/kotlin"))
        // sources
        staticFiles("/src", File("./yass2-ktor/src"))
        staticFiles("/", File("."))
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

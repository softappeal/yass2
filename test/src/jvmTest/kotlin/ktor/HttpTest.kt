package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.coroutines.acceptorSessionFactory
import ch.softappeal.yass2.coroutines.tunnel
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
    }
}

class HttpTest {
    @Test
    fun test() {
        Server.start()
        try {
            runBlocking {
                ktorClientTest(io.ktor.client.engine.cio.CIO)
            }
        } finally {
            Server.stop()
        }
    }
}

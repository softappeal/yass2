package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.core.remote.serverTunnel
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val Server = embeddedServer(io.ktor.server.cio.CIO, PORT) {
    install(WebSockets)
    routing {
        route(
            ContractSerializer,
            PATH,
            serverTunnel(
                {
                    val context = currentCoroutineContext()[CallCce]!!.call.request.headers[CONTEXT_HEADER]!!
                    assertTrue(context.startsWith(CONTEXT_VALUE))
                    "http-$context"
                },
                {
                    currentCoroutineContext()[CallCce]!!.call.response.headers.append(CONTEXT_HEADER, CONTEXT_VALUE)
                },
            ),
        )
        webSocket(PATH) {
            receiveLoop(
                ContractSerializer,
                acceptorSessionFactory {
                    val context = (connection.session as WebSocketServerSession).call.request.headers[CONTEXT_HEADER]!!
                    assertEquals(CONTEXT_VALUE, context)
                    "ws-$context"
                },
            )
        }
        // code
        staticFiles("/wasm", File("./build/wasm/packages/yass2-ktor-test/kotlin"))
        staticFiles("/js", File("./build/js/packages/yass2-ktor-test/kotlin"))
        // sources
        staticFiles("/wasm", File(".")) // wasm
        staticFiles("/", File("."))     // js
    }
}

class KtorTest {
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

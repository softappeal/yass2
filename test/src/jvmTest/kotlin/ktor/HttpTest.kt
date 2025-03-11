@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.contract.ContractTransport
import ch.softappeal.yass2.contract.DEMO_HEADER_KEY
import ch.softappeal.yass2.contract.DEMO_HEADER_VALUE
import ch.softappeal.yass2.contract.LOCAL_HOST
import ch.softappeal.yass2.contract.PATH
import ch.softappeal.yass2.coroutines.acceptorSessionFactory
import ch.softappeal.yass2.coroutines.initiatorSessionFactory
import ch.softappeal.yass2.coroutines.test
import ch.softappeal.yass2.coroutines.tunnel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.header
import io.ktor.http.headersOf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

private fun Application.httpModule() {
    routing {
        route(
            ContractTransport,
            PATH,
            tunnel { currentCoroutineContext()[CallCce]!!.call.request.headers[DEMO_HEADER_KEY]!! }
        )
    }
}

private fun Application.webSocketModule() {
    install(io.ktor.server.websocket.WebSockets)
    routing {
        webSocket(PATH) {
            receiveLoop(
                ContractTransport,
                acceptorSessionFactory { (connection.session as WebSocketServerSession).call.request.headers[DEMO_HEADER_KEY]!! }
            )
        }
    }
}

class HttpTest {
    @Test
    fun http() {
        val server = embeddedServer(io.ktor.server.cio.CIO, 0, module = Application::httpModule)
        server.start()
        try {
            runBlocking {
                val randomPort = server.engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO).use { client ->
                    client
                        .tunnel(ContractTransport, "http://$LOCAL_HOST:$randomPort$PATH") {
                            headersOf(DEMO_HEADER_KEY, DEMO_HEADER_VALUE)
                        }
                        .test(100)
                }
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun webSocket() {
        val server = embeddedServer(io.ktor.server.cio.CIO, 0, module = Application::webSocketModule)
        server.start()
        try {
            runBlocking {
                val randomPort = server.engine.resolvedConnectors().first().port
                HttpClient(io.ktor.client.engine.cio.CIO) {
                    install(io.ktor.client.plugins.websocket.WebSockets)
                }.use { client ->
                    client.ws("ws://$LOCAL_HOST:$randomPort$PATH", { header(DEMO_HEADER_KEY, DEMO_HEADER_VALUE) }) {
                        receiveLoop(ContractTransport, initiatorSessionFactory(1000))
                    }
                }
            }
        } finally {
            server.stop()
        }
    }
}

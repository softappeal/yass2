@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.ktor.receiveLoop
import ch.softappeal.yass2.ktor.route
import ch.softappeal.yass2.ktor.tunnel
import ch.softappeal.yass2.remote.tunnel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ws
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.runBlocking
import java.io.File

public const val LOCAL_HOST: String = "localhost"
public const val PORT: Int = 28947
private const val PATH = "/yass"

private fun Application.theModule() {
    install(io.ktor.server.websocket.WebSockets)
    routing {
        staticFiles("/", File("./build/js/packages/tutorial-wasm/kotlin"))
        staticFiles("/", File("./")) // needed for debugging (sources)
        staticFiles("/", File("./tutorial/")) // needed for debugging (sources)

        // shows server-side unidirectional remoting with Http
        route(ContractTransport, PATH, tunnel(
            CalculatorId.service(CalculatorImpl),
        ))

        // shows server-side session based bidirectional remoting with WebSocket
        webSocket(PATH) { receiveLoop(ContractTransport, acceptorSessionFactory()) }
    }
}

public fun createKtorServer(): EmbeddedServer<*, *> = embeddedServer(io.ktor.server.cio.CIO, PORT, module = Application::theModule)

private suspend fun useKtorRemoting() {
    println("*** useKtorRemoting ***")
    val server = createKtorServer()
    server.start()
    try {
        HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }.use { client ->
            // shows client-side unidirectional remoting with Http
            useServices(client.tunnel(ContractTransport, "http://$LOCAL_HOST:$PORT$PATH"))

            // shows client-side session based bidirectional remoting with WebSocket
            client.ws("ws://$LOCAL_HOST:$PORT$PATH") { receiveLoop(ContractTransport, initiatorSessionFactory()) }
        }
    } finally {
        server.stop()
    }
}

public fun main(): Unit = runBlocking {
    showUsage()
    useKtorRemoting()
}

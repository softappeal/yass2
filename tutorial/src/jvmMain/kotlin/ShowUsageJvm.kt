@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.ktor.*
import ch.softappeal.yass2.tutorial.contract.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import java.io.*

public const val LOCAL_HOST: String = "localhost"
public const val PORT: Int = 28947
private const val PATH = "/yass"

private fun Application.theModule() {
    install(io.ktor.server.websocket.WebSockets)
    routing {
        staticFiles("/", File("./")) // needed for debugging (sources)
        staticFiles("/", File("./build/js/packages/tutorial/kotlin"))

        // shows server-side unidirectional remoting with Http
        route(MessageTransport, PATH, tunnel(
            CalculatorId.service(CalculatorImpl),
            flowService(),
        ))

        // shows server-side session based bidirectional remoting with WebSocket
        webSocket(PATH) { receiveLoop(PacketTransport, acceptorSessionFactory()) }
    }
}

public fun createKtorEngine(): ApplicationEngine = embeddedServer(io.ktor.server.cio.CIO, PORT, module = Application::theModule)

private suspend fun useKtorRemoting() {
    println("*** useKtorRemoting ***")
    val engine = createKtorEngine()
    engine.start()
    try {
        HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }.use { client ->
            // shows client-side unidirectional remoting with Http
            useServices(client.tunnel(MessageTransport, "http://$LOCAL_HOST:$PORT$PATH"))

            // shows client-side session based bidirectional remoting with WebSocket
            client.ws("ws://$LOCAL_HOST:$PORT$PATH") { receiveLoop(PacketTransport, initiatorSessionFactory()) }
        }
    } finally {
        engine.stop()
    }
}

public fun main(): Unit = runBlocking {
    showUsage()
    useKtorRemoting()
}

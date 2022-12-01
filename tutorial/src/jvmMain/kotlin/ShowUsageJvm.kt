@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.ktor.*
import ch.softappeal.yass2.tutorial.contract.*
import ch.softappeal.yass2.tutorial.contract.generated.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.*

public const val Host: String = "localhost"
public const val Port: Int = 28947
private const val Path = "/yass"

private fun Application.theModule() {
    install(io.ktor.server.websocket.WebSockets)
    routing {
        static {
            files("./") // needed for debugging (sources)
            files("./tutorial/src/jsMain/resources")
            files("./build/js/node_modules")
        }

        // shows server-side unidirectional remoting with Http
        route(MessageTransport, Path, ::generatedInvoke.tunnel(Services))

        // shows server-side session based bidirectional remoting with WebSocket
        webSocket(Path) { receiveLoop(PacketTransport, acceptorSessionFactory()) }
    }
}

public fun createKtorEngine(): ApplicationEngine = embeddedServer(io.ktor.server.cio.CIO, Port, module = Application::theModule)

private suspend fun useKtorRemoting() {
    println("*** useKtorRemoting ***")
    val engine = createKtorEngine()
    engine.start()
    try {
        HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }.use { client ->
            // shows client-side unidirectional remoting with Http
            useServices(client.tunnel(MessageTransport, "http://$Host:$Port$Path"))

            // shows client-side session based bidirectional remoting with WebSocket
            client.ws(HttpMethod.Get, Host, Port, Path) { receiveLoop(PacketTransport, initiatorSessionFactory()) }
        }
    } finally {
        engine.stop()
    }
}

public fun main(): Unit = runBlocking {
    showUsage()
    useKtorRemoting()
}

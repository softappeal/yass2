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

private const val HOST: String = "localhost"
private const val PORT: Int = 28947
private const val PATH = "/yass"

private fun Application.theModule() {
    install(io.ktor.server.websocket.WebSockets)
    routing {
        // shows server-side unidirectional remoting with Http
        route(MessageTransport, PATH, ::generatedInvoke.tunnel(Services))

        // shows server-side session based bidirectional remoting with WebSocket
        webSocket(PATH) { receiveLoop(PacketTransport, acceptorSessionFactory()) }
    }
}

private suspend fun useKtorRemoting() {
    println("*** useKtorRemoting ***")
    val engine = embeddedServer(io.ktor.server.cio.CIO, PORT, module = Application::theModule)
    engine.start()
    try {
        HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }.use { client ->
            // shows client-side unidirectional remoting with Http
            useServices(client.tunnel(MessageTransport, "http://$HOST:$PORT$PATH"))

            // shows client-side session based bidirectional remoting with WebSocket
            client.ws(HttpMethod.Get, HOST, PORT, PATH) { receiveLoop(PacketTransport, initiatorSessionFactory()) }
        }
    } finally {
        engine.stop()
    }
}

public fun main(): Unit = runBlocking {
    showUsage()
    useKtorRemoting()
}

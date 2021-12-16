package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.transport.ktor.*
import ch.softappeal.yass2.tutorial.contract.generated.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.util.concurrent.*

fun main() = runBlocking {
    showGeneratedUsage()
    useKtorRemoting(::remoteProxyFactory, ::invoke)
}

const val Host = "localhost"
const val Port = 28947
private const val Path = "/yass"

fun createKtorEngine(invoker: Invoker): ApplicationEngine = embeddedServer(io.ktor.server.cio.CIO, Port) {
    install(io.ktor.websocket.WebSockets)
    routing {
        static {
            files("./") // needed for debugging (sources)
            files("./module/tutorial/src/jsMain/resources")
            files("./build/js/node_modules")
        }

        // shows server-side unidirectional remoting with Http
        route(MessageTransport, Path, invoker.tunnel(Services))

        // shows server-side session based bidirectional remoting with WebSocket
        webSocket(Path) { receiveLoop(PacketTransport, acceptorSessionFactory()) }
    }
}

private suspend fun useKtorRemoting(remoteProxyFactoryCreator: (tunnel: Tunnel) -> RemoteProxyFactory, invoker: Invoker) {
    println("*** useKtorRemoting ***")
    val engine = createKtorEngine(invoker)
    engine.start()
    try {
        HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.features.websocket.WebSockets)
        }.use { client ->
            // shows client-side unidirectional remoting with Http
            useServices(client.tunnel(MessageTransport, "http://$Host:$Port$Path"), remoteProxyFactoryCreator)

            // shows client-side session based bidirectional remoting with WebSocket
            client.ws(HttpMethod.Get, Host, Port, Path) { receiveLoop(PacketTransport, initiatorSessionFactory()) }
        }
    } finally {
        engine.stop(0, 0, TimeUnit.SECONDS)
    }
    delay(2_000) // needed for graceful shutdown
    println()
}

package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.coroutines.Connection
import ch.softappeal.yass2.coroutines.Session
import ch.softappeal.yass2.coroutines.SessionFactory
import ch.softappeal.yass2.ktor.Transport
import ch.softappeal.yass2.ktor.receiveLoop
import ch.softappeal.yass2.ktor.route
import ch.softappeal.yass2.ktor.tunnel
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.remote.tunnel
import ch.softappeal.yass2.serialize.string.StringSerializer
import ch.softappeal.yass2.serialize.string.readString
import ch.softappeal.yass2.serialize.string.writeString
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ws
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val CalculatorImpl: Calculator = object : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

private val NewsListenerImpl: NewsListener = object : NewsListener {
    override suspend fun notify(news: String) {
        println("NewsListener.notify: $news")
    }
}

private suspend fun useCalculator(calculator: Calculator) {
    println("1 + 2 = ${calculator.add(1, 2)}")
}

private suspend fun showUsage() {
    fun useSerializer(serializer: StringSerializer) {
        println("*** useSerializer ***")
        val serialized = serializer.writeString(MyDate(123456))
        println(serialized)
        println(serializer.readString(serialized))
    }

    suspend fun useInterceptor() {
        println("*** useInterceptor ***")
        val calculator = CalculatorImpl.proxy { function, _, invoke ->
            println("calling function '$function'")
            invoke()
        }
        useCalculator(calculator)
    }
    useSerializer(TutorialSerializer)
    useInterceptor()
}

private suspend fun useServices(tunnel: Tunnel) {
    val calculator = CalculatorId.proxy(tunnel)
    useCalculator(calculator)
}

// The following code is only needed if you use session based bidirectional remoting.

private fun <C : Connection> CoroutineScope.initiatorSessionFactory(): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = tunnel(NewsListenerId.service(NewsListenerImpl))

        override fun opened() {
            launch {
                useServices(clientTunnel)
                delay(100.milliseconds) // give the server some time to send news
                close()
            }
        }

        override suspend fun closed(e: Exception?) {
            println("initiatorSessionFactory closed: $e")
        }
    }
}

private fun <C : Connection> CoroutineScope.acceptorSessionFactory(): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = tunnel(
            CalculatorId.service(CalculatorImpl),
        )

        override fun opened() {
            launch {
                val newsListener = NewsListenerId.proxy(clientTunnel)
                newsListener.notify("News 1")
                newsListener.notify("News 2")
            }
        }

        override suspend fun closed(e: Exception?) {
            println("acceptorSessionFactory closed: $e")
        }
    }
}

private val TutorialTransport = Transport(TutorialSerializer)

private const val LOCAL_HOST = "localhost"
private const val PORT = 28947
private const val PATH = "/yass"

private fun Application.theModule() {
    install(io.ktor.server.websocket.WebSockets)
    routing {
        // shows server-side unidirectional remoting with Http
        route(TutorialTransport, PATH, tunnel(
            CalculatorId.service(CalculatorImpl),
        ))

        // shows server-side session based bidirectional remoting with WebSocket
        webSocket(PATH) { receiveLoop(TutorialTransport, acceptorSessionFactory()) }
    }
}

private suspend fun useKtorRemoting() {
    println("*** useKtorRemoting ***")
    val server = embeddedServer(io.ktor.server.cio.CIO, PORT, module = Application::theModule)
    server.start()
    try {
        HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }.use { client ->
            // shows client-side unidirectional remoting with Http
            @Suppress("HttpUrlsUsage")
            useServices(client.tunnel(TutorialTransport, "http://$LOCAL_HOST:$PORT$PATH"))

            // shows client-side session based bidirectional remoting with WebSocket
            client.ws("ws://$LOCAL_HOST:$PORT$PATH") { receiveLoop(TutorialTransport, initiatorSessionFactory()) }
        }
    } finally {
        server.stop()
    }
}

public suspend fun main() {
    showUsage()
    useKtorRemoting()
}

package tutorial

import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.core.serialize.string.StringSerializer
import ch.softappeal.yass2.core.serialize.string.fromString
import ch.softappeal.yass2.core.serialize.string.toString
import ch.softappeal.yass2.coroutines.session.Connection
import ch.softappeal.yass2.coroutines.session.Session
import ch.softappeal.yass2.ktor.WebSocketConnection
import ch.softappeal.yass2.ktor.receiveLoop
import ch.softappeal.yass2.ktor.route
import ch.softappeal.yass2.ktor.tunnel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ws
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

fun useSerializer(serializer: StringSerializer) {
    println("*** useSerializer ***")
    val serialized = serializer.toString(MyDate(123456))
    println(serialized)
    println(serializer.fromString(serialized))
}

object CalculatorImpl : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

suspend fun useCalculator(calculator: Calculator) {
    println("1 + 2 = ${calculator.add(1, 2)}")
}

suspend fun useInterceptor() {
    println("*** useInterceptor ***")
    val calculator = CalculatorImpl.proxy { function, _, invoke ->
        println("calling function '$function'")
        invoke()
    }
    useCalculator(calculator)
}

suspend fun useTunnel(tunnel: Tunnel) {
    val calculator = CalculatorId.proxy(tunnel)
    useCalculator(calculator)
}

abstract class InitiatorSession<C : Connection> : Session<C>() {
    fun printNews(news: String) {
        println("news: $news")
    }
}

// Shows how to pass session to service implementation.
class NewsListenerImpl<C : Connection>(val session: InitiatorSession<C>) : NewsListener {
    override suspend fun notify(news: String) {
        session.printNews(news)
    }
}

fun CoroutineScope.initiatorSessionFactory() = {
    object : InitiatorSession<WebSocketConnection>() {
        override val serverTunnel = tunnel(
            NewsListenerId.service(NewsListenerImpl(this)),
        )

        override fun opened() {
            launch {
                useTunnel(clientTunnel)
                delay(100.milliseconds) // give the server some time to send news
                close()
            }
        }

        override suspend fun closed(e: Exception?) {
            println("initiatorSessionFactory closed: $e")
        }
    }
}

fun CoroutineScope.acceptorSessionFactory() = {
    object : Session<WebSocketConnection>() {
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

suspend fun useKtor() {
    println("*** useKtor ***")

    val localHost = "localhost"
    val port = 28947
    val path = "/yass"

    suspend fun client() {
        HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }.use { client ->
            @Suppress("HttpUrlsUsage") useTunnel(
                client.tunnel(TutorialSerializer, "http://$localHost:$port$path")
            )
            client.ws("ws://$localHost:$port$path") { receiveLoop(TutorialSerializer, initiatorSessionFactory()) }
        }
    }

    val server = embeddedServer(io.ktor.server.cio.CIO, port) {
        install(io.ktor.server.websocket.WebSockets)
        routing {
            route(
                TutorialSerializer,
                path,
                tunnel(
                    CalculatorId.service(CalculatorImpl),
                )
            )
            webSocket(path) { receiveLoop(TutorialSerializer, acceptorSessionFactory()) }
        }
    }

    server.start()
    try {
        client()
    } finally {
        server.stop()
    }
}

suspend fun main() {
    useSerializer(TutorialSerializer)
    useInterceptor()
    useKtor()
}

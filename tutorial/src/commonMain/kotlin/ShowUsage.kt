package tutorial

import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.core.serialize.string.StringSerializer
import ch.softappeal.yass2.core.serialize.string.fromString
import ch.softappeal.yass2.core.serialize.string.toString
import ch.softappeal.yass2.coroutines.session.Connection
import ch.softappeal.yass2.coroutines.session.Session
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

private fun useSerializer(serializer: StringSerializer) {
    println("*** useSerializer ***")
    val serialized = serializer.toString(MyDate(123456))
    println(serialized)
    println(serializer.fromString(serialized))
}

private object CalculatorImpl : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

private object NewsListenerImpl : NewsListener {
    override suspend fun notify(news: String) {
        println("NewsListener.notify: $news")
    }
}

private suspend fun useCalculator(calculator: Calculator) {
    println("1 + 2 = ${calculator.add(1, 2)}")
}

private suspend fun useInterceptor() {
    println("*** useInterceptor ***")
    val calculator = CalculatorImpl.proxy { function, _, invoke ->
        println("calling function '$function'")
        invoke()
    }
    useCalculator(calculator)
}

private suspend fun useTunnel(tunnel: Tunnel) {
    val calculator = CalculatorId.proxy(tunnel)
    useCalculator(calculator)
}

private fun <C : Connection> CoroutineScope.initiatorSessionFactory() = {
    object : Session<C>() {
        override val serverTunnel = tunnel(
            NewsListenerId.service(NewsListenerImpl),
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

private fun <C : Connection> CoroutineScope.acceptorSessionFactory() = {
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

private suspend fun useKtor() {
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

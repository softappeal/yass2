package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.coroutines.Connection
import ch.softappeal.yass2.coroutines.Session
import ch.softappeal.yass2.coroutines.SessionFactory
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.remote.tunnel
import ch.softappeal.yass2.serialize.BytesReader
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.Serializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

public val CalculatorImpl: Calculator = object : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

public val NewsListenerImpl: NewsListener = object : NewsListener {
    override suspend fun notify(news: String) {
        println("NewsListener.notify: $news")
    }
}

private suspend fun useCalculator(calculator: Calculator) {
    println("1 + 2 = ${calculator.add(1, 2)}")
}

public suspend fun showUsage() {
    fun useSerializer(serializer: Serializer) {
        println("*** useSerializer ***")
        val writer = BytesWriter(100)
        serializer.write(writer, MyDate(123456))
        val reader = BytesReader(writer.buffer)
        val value = serializer.read(reader)
        println(value)
    }

    suspend fun useInterceptor() {
        println("*** useInterceptor ***")
        val calculator = CalculatorImpl.proxy { function, _, invoke ->
            println("calling function '$function'")
            invoke()
        }
        useCalculator(calculator)
    }
    useSerializer(TransportSerializer)
    useInterceptor()
}

public suspend fun useServices(tunnel: Tunnel) {
    val calculator = CalculatorId.proxy(tunnel)
    useCalculator(calculator)
}

// The following code is only needed if you use session based bidirectional remoting.

public fun <C : Connection> CoroutineScope.initiatorSessionFactory(): SessionFactory<C> = {
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

public fun <C : Connection> CoroutineScope.acceptorSessionFactory(): SessionFactory<C> = {
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

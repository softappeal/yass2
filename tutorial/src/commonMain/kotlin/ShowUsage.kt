package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*
import ch.softappeal.yass2.tutorial.contract.*
import ch.softappeal.yass2.tutorial.contract.generated.*
import kotlinx.coroutines.*

public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

public val MessageTransport: Transport = Transport(MessageSerializer, 100)
public val PacketTransport: Transport = Transport(PacketSerializer, 100)

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
    fun useDumper(dumper: Dumper) {
        println("*** useDumper ***")
        val person = Person(
            "Guru",
            Gender.Female,
            listOf(
                Address("Infinity Drive").apply { number = 1 },
                Address("Hollywood Boulevard")
            )
        )
        println(StringBuilder().dumper(person))
    }

    fun useSerializer(serializer: Serializer) {
        println("*** useSerializer ***")
        val writer = BytesWriter(100)
        serializer.write(writer, "hello")
        val reader = BytesReader(writer.buffer)
        val value = serializer.read(reader)
        println(value)
    }

    suspend fun useInterceptor(proxyFactory: ProxyFactory) {
        println("*** useInterceptor ***")
        val interceptor: SuspendInterceptor = { function, _, invocation ->
            println("calling function '${function.name}'")
            invocation()
        }
        val calculator = proxyFactory(CalculatorImpl, interceptor)
        useCalculator(calculator)
    }
    useDumper(dumper)
    useSerializer(ContractSerializer)
    useInterceptor(GeneratedProxyFactory)
}

public suspend fun useServices(tunnel: Tunnel) {
    val remoteProxyFactory = generatedRemoteProxyFactory(tunnel)
    val calculator = remoteProxyFactory(CalculatorId)
    useCalculator(calculator)
}

public val Services: List<Service> = listOf(CalculatorId(CalculatorImpl)) // register services

// The following code is only needed if you use session based bidirectional remoting.

public fun CoroutineScope.initiatorSessionFactory(): SessionFactory = {
    object : Session() {
        override val serverTunnel = ::generatedInvoke.tunnel(listOf(
            NewsListenerId(NewsListenerImpl) // register service
        ))

        override fun opened() {
            launch {
                useServices(clientTunnel)
                delay(100) // give the server some time to send news
                close()
            }
        }

        override suspend fun closed(e: Exception?) {
            println("initiatorSessionFactory closed: $e")
        }
    }
}

public fun CoroutineScope.acceptorSessionFactory(): SessionFactory = {
    object : Session() {
        override val serverTunnel = ::generatedInvoke.tunnel(Services)

        override fun opened() {
            launch {
                val remoteProxyFactory = generatedRemoteProxyFactory(clientTunnel)
                val newsListener = remoteProxyFactory(NewsListenerId)
                newsListener.notify("News 1")
                newsListener.notify("News 2")
            }
        }

        override suspend fun closed(e: Exception?) {
            println("acceptorSessionFactory closed: $e")
        }
    }
}

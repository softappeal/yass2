package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.tutorial.contract.*
import ch.softappeal.yass2.tutorial.contract.generated.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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
    useDumper(Dumper)
    useSerializer(TutorialBinarySerializer)
    useInterceptor(TutorialProxyFactory)
}

public suspend fun useServices(tunnel: Tunnel) {
    val remoteProxyFactory = tutorialRemoteProxyFactory(tunnel)
    val calculator = remoteProxyFactory(CalculatorId)
    useCalculator(calculator)
    val flowService = remoteProxyFactory(FlowServiceId)
    val booleanFlow = flowService.createFlow<Boolean>(BooleanFlowId())
    println(booleanFlow.toList())
    val intFlow = flowService.createFlow<Int>(IntFlowId(10))
    println(intFlow.toList())
}

public fun flowService(): Service {
    val flowFactory = { flowId: FlowId ->
        when (flowId) {
            is IntFlowId -> (1..flowId.max).asFlow()
            is BooleanFlowId -> flowOf(false, true)
        }
    }
    @Suppress("UNCHECKED_CAST") return FlowServiceId(flowService(flowFactory as FlowFactory))
}

// The following code is only needed if you use session based bidirectional remoting.

public fun <C : Connection> CoroutineScope.initiatorSessionFactory(): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = ::tutorialInvoke.tunnel(NewsListenerId(NewsListenerImpl))

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

public fun <C : Connection> CoroutineScope.acceptorSessionFactory(): SessionFactory<C> = {
    object : Session<C>() {
        override val serverTunnel = ::tutorialInvoke.tunnel(
            CalculatorId(CalculatorImpl),
            flowService(),
        )

        override fun opened() {
            launch {
                val remoteProxyFactory = tutorialRemoteProxyFactory(clientTunnel)
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

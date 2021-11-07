package ch.softappeal.yass2.remote

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.contract.generated.ProxyFactory
import ch.softappeal.yass2.remote.coroutines.*
import kotlin.test.*

private val RemoteProxyFactory = (::remoteProxyFactoryCreator)(::invoker.tunnel(listOf(CalculatorId(CalculatorImpl), EchoId(EchoImpl))))

class RemoteTest {
    @Test
    fun duplicatedServices() {
        assertEquals(
            "duplicated service id's",
            assertFailsWith<IllegalArgumentException> {
                ::invoker.tunnel(listOf(EchoId(EchoImpl), EchoId(EchoImpl)))
            }.message
        )
    }

    @Test
    fun missingService() = yassRunBlocking {
        assertEquals(
            "no service id 1",
            assertFailsWith<IllegalStateException> {
                remoteProxyFactoryCreator(::invoker.tunnel(emptyList()))(CalculatorId).add(1, 2)
            }.message
        )
    }

    @Test
    fun noServiceIdRemoteProxyFactory() {
        assertEquals(
            "no service id 123",
            assertFailsWith<IllegalStateException> {
                remoteProxyFactoryCreator { ValueReply(null) }.create(serviceId<Calculator>(123))
            }.message
        )
    }

    @Test
    fun noServiceIdInvoker() = yassRunBlocking {
        assertEquals(
            "no service id 123",
            assertFailsWith<IllegalStateException> {
                invoker(Request(123, 0, emptyList()), EchoId(EchoImpl))
            }.message
        )
    }

    @Test
    fun noFunctionId() = yassRunBlocking {
        assertEquals(
            "no function id 123 for service id 2",
            assertFailsWith<IllegalStateException> {
                invoker(Request(2, 123, emptyList()), EchoId(EchoImpl))
            }.message
        )
    }

    @Test
    fun exceptionReply() {
        val exception = Exception()
        assertSame(exception, ExceptionReply(exception).exception)
    }

    @Test
    fun valueReply() {
        val value = 123
        assertSame(value, ValueReply(value).value)
    }

    @Test
    fun test() = yassRunBlocking {
        ProxyFactory.test(RemoteProxyFactory(CalculatorId), RemoteProxyFactory(EchoId))
    }

    @Test
    fun performance() = yassRunBlocking {
        val calculator = RemoteProxyFactory(CalculatorId)
        performance(100_000) { assertEquals(5, calculator.add(2, 3)) }
    }
}

fun tunnel(context: suspend () -> Any): Tunnel = ::invoker.tunnel(listOf(
    CalculatorId(CalculatorImpl),
    EchoId(ProxyFactory(EchoImpl) { _, _, invocation: Invocation ->
        println("context<${context()}>")
        invocation()
    }),
    FlowServiceId(FlowServiceImpl)
))

suspend fun Tunnel.test(iterations: Int): Unit = with(remoteProxyFactoryCreator(this)) {
    val calculator = this(CalculatorId)
    ProxyFactory.test(calculator, this(EchoId))
    performance(iterations) { assertEquals(5, calculator.add(2, 3)) }
    this(FlowServiceId).test()
}

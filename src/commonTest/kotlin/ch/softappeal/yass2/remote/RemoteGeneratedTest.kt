package ch.softappeal.yass2.remote

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.*

class RemoteTest {
    @Test
    fun duplicatedServices() {
        assertEquals(
            "duplicated service id's",
            assertFailsWith<IllegalArgumentException> {
                ::generatedInvoker.tunnel(listOf(EchoId(EchoImpl), EchoId(EchoImpl)))
            }.message
        )
    }

    @Test
    fun missingService() = yassRunBlocking {
        assertEquals(
            "no service id 1",
            assertFailsWith<IllegalStateException> {
                generatedRemoteProxyFactoryCreator(::generatedInvoker.tunnel(emptyList()))(CalculatorId).add(1, 2)
            }.message
        )
    }

    @Test
    fun noServiceIdRemoteProxyFactory() {
        assertEquals(
            "no service id 123",
            assertFailsWith<IllegalStateException> {
                generatedRemoteProxyFactoryCreator { ValueReply(null) }.create(serviceId<Calculator>(123))
            }.message
        )
    }

    @Test
    fun noServiceIdInvoker() = yassRunBlocking {
        assertEquals(
            "no service id 123",
            assertFailsWith<IllegalStateException> {
                generatedInvoker(Request(123, 0, emptyList()), EchoId(EchoImpl))
            }.message
        )
    }

    @Test
    fun noFunctionId() = yassRunBlocking {
        assertEquals(
            "no function id 123 for service id 2",
            assertFailsWith<IllegalStateException> {
                generatedInvoker(Request(2, 123, emptyList()), EchoId(EchoImpl))
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
}

val Services = listOf(CalculatorId(CalculatorImpl), EchoId(EchoImpl))

suspend fun RemoteProxyFactory.test(): Unit = GeneratedProxyFactory.test(this(CalculatorId), this(EchoId))

open class RemoteGeneratedTest {
    protected open val remoteProxyFactoryCreator: RemoteProxyFactoryCreator = ::generatedRemoteProxyFactoryCreator
    protected open val invoker: Invoker = ::generatedInvoker
    private fun remoteProxyFactory(): RemoteProxyFactory = remoteProxyFactoryCreator(invoker.tunnel(Services))

    @Test
    fun generated() = yassRunBlocking {
        remoteProxyFactory().test()
    }

    @Test
    fun performance() = yassRunBlocking {
        val calculator = remoteProxyFactory()(CalculatorId)
        performance(100_000) { assertEquals(5, calculator.add(2, 3)) }
    }
}

fun tunnel(context: suspend () -> Any) = ::generatedInvoker.tunnel(listOf(
    CalculatorId(CalculatorImpl),
    EchoId(GeneratedProxyFactory(EchoImpl) { _, _, invocation: SuspendInvocation ->
        println("context<${context()}>")
        invocation()
    }),
    FlowServiceId(flowService { flowId ->
        flow {
            for (i in 1..flowId as Int) {
                delay(1000)
                println("emitting $i")
                emit(i)
            }
        }
    })
))

suspend fun Tunnel.test(iterations: Int) = with(generatedRemoteProxyFactoryCreator(this)) {
    val calculator = this(CalculatorId)
    GeneratedProxyFactory.test(calculator, this(EchoId))
    performance(iterations) { assertEquals(5, calculator.add(2, 3)) }
    val flowService = this(FlowServiceId)
    flowService
        .createFlow<Int>(2)
        .collect { value -> println("collect($value)") }
}

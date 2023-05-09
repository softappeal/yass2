package ch.softappeal.yass2.remote

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class RemoteTest {
    @Test
    fun duplicatedService() = assertFailsMessage<IllegalArgumentException>("duplicated service id") {
        ::generatedInvoke.tunnel(EchoId(EchoImpl), EchoId(EchoImpl))
    }

    @Test
    fun missingService() = runTest {
        assertFailsMessage<IllegalStateException>("no service id 1") {
            generatedRemoteProxyFactory(::generatedInvoke.tunnel())(CalculatorId).add(1, 2)
        }
    }

    @Test
    fun noServiceIdRemoteProxyFactory() = assertFailsMessage<IllegalStateException>("no service id 123") {
        generatedRemoteProxyFactory { ValueReply(null) }.create(serviceId<Calculator>(123))
    }

    @Test
    fun noServiceIdInvoker() = runTest {
        assertFailsMessage<IllegalStateException>("no service id 123") {
            generatedInvoke(Request(123, 0, emptyList()), EchoId(EchoImpl))
        }
    }

    @Test
    fun noFunctionId() = runTest {
        assertFailsMessage<IllegalStateException>("no function id 123 for service id 2") {
            generatedInvoke(Request(2, 123, emptyList()), EchoId(EchoImpl))
        }
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

    private val remoteProxyFactory = generatedRemoteProxyFactory(::generatedInvoke.tunnel(CalculatorId(CalculatorImpl), EchoId(EchoImpl)))

    @Test
    fun test() = runTest {
        test(remoteProxyFactory(CalculatorId), remoteProxyFactory(EchoId))
    }

    @Test
    fun performance() = runTest {
        val calculator = remoteProxyFactory(CalculatorId)
        performance(100_000) { assertEquals(5, calculator.add(2, 3)) }
    }
}

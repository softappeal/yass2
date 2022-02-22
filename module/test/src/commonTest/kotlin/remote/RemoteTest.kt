package ch.softappeal.yass2.remote

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import kotlin.test.*

open class RemoteTest {
    protected open val invoke = ::generatedInvoke
    protected open val remoteProxyFactory = ::generatedRemoteProxyFactory

    @Test
    fun duplicatedService() {
        assertEquals(
            "duplicated service id",
            assertFailsWith<IllegalArgumentException> {
                invoke.tunnel(listOf(EchoId(EchoImpl), EchoId(EchoImpl)))
            }.message
        )
    }

    @Test
    fun missingService() = yassRunBlocking {
        assertEquals(
            "no service id 1",
            assertFailsWith<IllegalStateException> {
                remoteProxyFactory(invoke.tunnel(emptyList()))(CalculatorId).add(1, 2)
            }.message
        )
    }

    @Test
    fun noServiceIdRemoteProxyFactory() {
        assertEquals(
            "no service id 123",
            assertFailsWith<IllegalStateException> {
                generatedRemoteProxyFactory { ValueReply(null) }.create(serviceId<Calculator>(123))
            }.message
        )
    }

    @Test
    fun noServiceIdInvoker() = yassRunBlocking {
        assertEquals(
            "no service id 123",
            assertFailsWith<IllegalStateException> {
                generatedInvoke(Request(123, 0, emptyList()), EchoId(EchoImpl))
            }.message
        )
    }

    @Test
    fun noFunctionId() = yassRunBlocking {
        assertEquals(
            "no function id 123 for service id 2",
            assertFailsWith<IllegalStateException> {
                generatedInvoke(Request(2, 123, emptyList()), EchoId(EchoImpl))
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

    private fun remoteProxyFactory() = remoteProxyFactory(invoke.tunnel(listOf(CalculatorId(CalculatorImpl), EchoId(EchoImpl))))

    @Test
    fun test() = yassRunBlocking {
        GeneratedProxyFactory.test(remoteProxyFactory()(CalculatorId), remoteProxyFactory()(EchoId))
    }

    @Test
    fun performance() = yassRunBlocking {
        val calculator = remoteProxyFactory()(CalculatorId)
        performance(100_000) { assertEquals(5, calculator.add(2, 3)) }
    }
}

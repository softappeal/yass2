package ch.softappeal.yass2.core.remote

import ch.softappeal.yass2.contract.CalculatorId
import ch.softappeal.yass2.contract.Echo
import ch.softappeal.yass2.contract.EchoId
import ch.softappeal.yass2.contract.proxy
import ch.softappeal.yass2.contract.service
import ch.softappeal.yass2.core.CalculatorImpl
import ch.softappeal.yass2.core.EchoImpl
import ch.softappeal.yass2.core.assertFailsMessage
import ch.softappeal.yass2.core.performance
import ch.softappeal.yass2.core.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

private val TestTunnel = tunnel(CalculatorId.service(CalculatorImpl), EchoId.service(EchoImpl))

class RemoteTest {
    @Test
    fun duplicatedService() = assertFailsMessage<IllegalArgumentException>("duplicated service") {
        tunnel(EchoId.service(EchoImpl), EchoId.service(EchoImpl))
    }

    @Test
    fun noService() = runTest {
        assertFailsMessage<IllegalStateException>("no service '123'") {
            CalculatorId.proxy(TestTunnel)
            TestTunnel(Request("123", "0", listOf()))
        }
    }

    @Test
    fun noFunction() = runTest {
        assertFailsMessage<IllegalStateException>("service 'calc' has no function 'noParametersNoResult'") {
            ServiceId<Echo>(CalculatorId.id).proxy(tunnel(CalculatorId.service(CalculatorImpl))).noParametersNoResult()
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
        assertEquals(value, ValueReply(value).value)
    }

    @Test
    fun test() = runTest {
        test(CalculatorId.proxy(TestTunnel), EchoId.proxy(TestTunnel))
    }

    @Test
    fun performance() = runTest {
        val calculator = CalculatorId.proxy(TestTunnel)
        performance(100_000) { assertEquals(5, calculator.add(2, 3)) }
    }
}

fun tunnel(context: suspend () -> Any): Tunnel = tunnel(
    CalculatorId.service(CalculatorImpl),
    EchoId.service(EchoImpl.proxy { _, _, invoke ->
        println("context<${context()}>")
        invoke()
    }),
)

suspend fun Tunnel.test() {
    test(CalculatorId.proxy(this), EchoId.proxy(this))
}

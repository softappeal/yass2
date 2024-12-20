package ch.softappeal.yass2.remote

import ch.softappeal.yass2.CalculatorImpl
import ch.softappeal.yass2.EchoImpl
import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.CalculatorId
import ch.softappeal.yass2.contract.Echo
import ch.softappeal.yass2.contract.EchoId
import ch.softappeal.yass2.contract.reflect.proxy
import ch.softappeal.yass2.contract.reflect.service
import ch.softappeal.yass2.performance
import ch.softappeal.yass2.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

private val TestTunnel = tunnel(CalculatorId.service(CalculatorImpl), EchoId.service(EchoImpl))

class RemoteTest {
    @Test
    fun duplicatedService() = assertFailsMessage<IllegalArgumentException>("duplicated service id") {
        tunnel(EchoId.service(EchoImpl), EchoId.service(EchoImpl))
    }

    @Test
    fun noService() = runTest {
        assertFailsMessage<IllegalStateException>("no service with id 123") {
            CalculatorId.proxy(TestTunnel)
            TestTunnel(Request(123, 0, listOf()))
        }
    }

    @Test
    fun noFunction() = runTest {
        assertFailsMessage<IllegalStateException>("service with id 1 has no function with id 4") {
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

package ch.softappeal.yass2.core.remote

import ch.softappeal.yass2.CalculatorId
import ch.softappeal.yass2.Echo
import ch.softappeal.yass2.EchoId
import ch.softappeal.yass2.core.CalculatorImpl
import ch.softappeal.yass2.core.EchoImpl
import ch.softappeal.yass2.core.assertFailsWithMessage
import ch.softappeal.yass2.core.invoke
import ch.softappeal.yass2.proxy
import ch.softappeal.yass2.service
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.fail

suspend fun Tunnel.invoke() {
    invoke(CalculatorId.proxy(this), EchoId.proxy(this))
}

class RemoteTest {
    @Test
    fun duplicatedService() = assertFailsWithMessage<IllegalArgumentException>("duplicated service") {
        val service = Service("service") { _, _ -> null }
        tunnel(service, service)
    }

    @Test
    fun noService() = runTest {
        val request = Request("invalid", "add", listOf(1, 2))
        assertFailsWithMessage<IllegalStateException>("no service '${request.service}'") {
            tunnel(Service("service") { _, _ -> null })(request)
        }
    }

    @Test
    fun tunnel() = runTest {
        val exception = Exception()
        val tunnel = tunnel(
            Service("service1") { function, parameters ->
                when (function) {
                    "add" -> (parameters[0] as Int) + (parameters[1] as Int)
                    "null" -> null
                    "unit" -> Unit
                    "exception" -> throw exception
                    else -> fail(function)
                }
            },
            Service("service2") { _, _ -> null },
        )
        assertEquals(3, tunnel(Request("service1", "add", listOf(1, 2))).process())
        assertNull(tunnel(Request("service1", "null", listOf())).process())
        assertNull(tunnel(Request("service1", "unit", listOf())).process())
        assertSame(
            exception,
            assertFails { tunnel(Request("service1", "exception", listOf())).process() },
        )
    }

    @Test
    fun noFunction() = runTest {
        assertFailsWithMessage<IllegalStateException>("service 'calc' has no function 'noParametersNoResult'") {
            ServiceId<Echo>(CalculatorId.id).proxy(tunnel(CalculatorId.service(CalculatorImpl))).noParametersNoResult()
        }
    }

    @Test
    fun invoke() = runTest {
        tunnel(CalculatorId.service(CalculatorImpl), EchoId.service(EchoImpl)).invoke()
    }
}

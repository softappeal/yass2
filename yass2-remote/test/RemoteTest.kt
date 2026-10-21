package ch.softappeal.yass2.remote

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.fail

suspend fun Tunnel.clientTest(testMode: TestMode, interceptor: String) {
    CalculatorId.proxy(this).proxy(printer(interceptor)).interceptorTest(testMode)
}

fun serverTunnel(interceptor: String) = tunnel(
    CalculatorId.service(CalculatorImpl.proxy(printer(interceptor)))
)

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
    fun tunnelTest() = runTest {
        val exception = DivideByZeroException()
        val tunnel = tunnel(
            Service("service1") { function, parameters ->
                when (function) {
                    "add" -> (parameters[0] as Int) + (parameters[1] as Int)
                    "null" -> null
                    "unit" -> Unit
                    "exception" -> throw exception
                    "error" -> error("error")
                    else -> fail(function)
                }
            },
            Service("service2") { _, _ -> null },
        )
        assertEquals(3, tunnel(Request("service1", "add", listOf(1, 2))).process())
        assertNull(tunnel(Request("service1", "null", listOf())).process())
        assertNull(tunnel(Request("service1", "unit", listOf())).process())
        assertSame(exception, assertFails { tunnel(Request("service1", "exception", listOf())).process() })
        assertFailsWithMessage<IllegalStateException>("error") {
            tunnel(Request("service1", "error", listOf()))
        }
    }

    /* TODO
    @Test
    fun noFunction() = runTest {
        assertFailsWithMessage<IllegalStateException>("service 'Calculator' has no function 'service'") {
            ServiceId<GenericService<Int, Int, Int>>(CalculatorId.id).proxy(tunnel(CalculatorId.service(CalculatorImpl)))
                .service(1, 2)
        }
    }
     */

    @Test
    fun clientTest() = runTest {
        val tunnel = serverTunnel("server")
        tunnel.clientTest(TestMode.Normal, "client")
        tunnel.clientTest(TestMode.Exception, "client")
    }
}

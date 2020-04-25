package ch.softappeal.yass2.remote.session

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.remote.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.*

fun CoroutineScope.flowInitiatorSessionFactory(): SessionFactory = {
    object : Session() {
        override fun opened() {
            launch {
                val remoteProxyFactory = generatedRemoteProxyFactoryCreator(clientTunnel)
                val flowService = remoteProxyFactory(FlowServiceId)
                val flow = flowService.createFlow<Int>("flowId")
                coroutineScope {
                    launch {
                        flow.collect { value -> println("collect($value)") }
                    }
                }
                close()
            }
        }

        override suspend fun closed(e: Exception?) = println("initiatorSessionFactory closed: $e")
    }
}

fun flowAcceptorSessionFactory(): SessionFactory = {
    object : Session() {
        override val serverTunnel = ::generatedInvoker.tunnel(listOf(
            FlowServiceId(flowService { flowId ->
                when (flowId) {
                    "flowId" -> (1..10).asFlow()
                    else -> error("unexpected flowId")
                }
            })
        ))

        override suspend fun closed(e: Exception?) = println("acceptorSessionFactory closed: $e")
    }
}

class RemoteFlowTest {
    @Test
    fun test() = yassRunBlocking {
        val range1 = 1..1000
        val range2 = 2000..2500
        val flowService = flowService { flowId ->
            (if (flowId == 1) range1 else range2).asFlow()
        }
        val flow1 = flowService.createFlow<Int>(1)
        val flow2 = flowService.createFlow<Int>(2)
        coroutineScope {
            repeat(2) {
                launch { assertEquals(range1.toList(), flow1.toList()) }
                launch { assertEquals(range2.toList(), flow2.toList()) }
            }
        }
    }
}

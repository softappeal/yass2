@file:OptIn(ExperimentalApi::class)

package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.proxy
import ch.softappeal.yass2.contract.service
import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.Printer
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.tunnel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val range1 = 1..3
private val range2 = 11..12

private val flowFactory: FlowFactory<Int, Int> = { flowId ->
    when (flowId) {
        1 -> range1.asFlow()
        2 -> range2.asFlow()
        3 -> flow { throw DivideByZeroException() }
        else -> error("unexpected flowId $flowId")
    }
}

private val flowService = flowService(flowFactory).proxy(Printer)

private val flowServiceId = ServiceId<FlowService<Int, Int>>("flow")
private val remoteFlowService = flowServiceId.proxy(tunnel(flowServiceId.service(flowService)))

private val flow1 = remoteFlowService.createFlow(1)
private val flow2 = remoteFlowService.createFlow(2)
private val flow3 = remoteFlowService.createFlow(3)
private val flow4 = remoteFlowService.createFlow(4)

class RemoteFlowTest {
    @Test
    fun missingFlowId() = runTest {
        assertFailsWith<IllegalStateException> { flow4.toList() }
    }

    @Test
    fun flowException() = runTest {
        assertFailsWith<DivideByZeroException> { flow3.toList() }
    }

    @Test
    fun collect() = runTest {
        assertEquals(range1.toList(), flow1.toList())
    }

    @Test
    fun collectException() = runTest {
        class CollectException : RuntimeException()
        assertFailsWith<CollectException> {
            flow1.collect { throw CollectException() }
        }
    }

    @Test
    fun multiple() = runTest {
        repeat(2) {
            launch {
                delay(1_000)
                assertEquals(range1.toList(), flow1.onEach { delay(13_000) }.toList())
            }
            launch {
                delay(3_000)
                assertEquals(range2.toList(), flow2.onEach { delay(17_000) }.toList())
            }
        }
    }
}

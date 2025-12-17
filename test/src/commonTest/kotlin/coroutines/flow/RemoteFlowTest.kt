@file:OptIn(ExperimentalApi::class)

package ch.softappeal.yass2.coroutines.flow

import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.core.ExperimentalApi
import ch.softappeal.yass2.core.Printer
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.tunnel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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

private fun flowFactory(flowId: Int) = when (flowId) {
    1 -> range1.asFlow()
    2 -> range2.asFlow()
    3 -> flow { throw DivideByZeroException() }
    else -> error("unexpected flowId $flowId")
}

private val FlowServiceId = ServiceId<FlowService<Int, Int>>("flow")

private fun CoroutineScope.createFlow(flowId: Int): Flow<Int> {
    val flowService = flowService(::flowFactory).proxy(Printer)
    val remoteFlowService = FlowServiceId.proxy(tunnel(FlowServiceId.service(flowService)))
    return remoteFlowService.createFlow(flowId)
}

class RemoteFlowTest {
    @Test
    fun missingFlowId() = runTest {
        assertFailsWith<IllegalStateException> { createFlow(4).toList() }
    }

    @Test
    fun flowException() = runTest {
        assertFailsWith<DivideByZeroException> { createFlow(3).toList() }
    }

    @Test
    fun collect() = runTest {
        assertEquals(range1.toList(), createFlow(1).toList())
    }

    @Test
    fun collectException() = runTest {
        class CollectException : RuntimeException()
        assertFailsWith<CollectException> {
            createFlow(1).collect { throw CollectException() }
        }
    }

    @Test
    fun multiple() = runTest {
        repeat(2) {
            launch {
                delay(1_000)
                assertEquals(range1.toList(), createFlow(1).onEach { delay(13_000) }.toList())
            }
            launch {
                delay(3_000)
                assertEquals(range2.toList(), createFlow(2).onEach { delay(17_000) }.toList())
            }
        }
    }
}

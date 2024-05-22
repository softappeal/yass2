package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.Printer
import ch.softappeal.yass2.assertSuspendFailsWith
import ch.softappeal.yass2.contract.DivideByZeroException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RemoteFlowTest {
    @Test
    fun test() = runTest {
        val range1 = 1..10
        val range2 = 2000..2020

        val flowFactory: FlowFactory<Int, Int> = { flowId ->
            when (flowId) {
                1 -> range1.asFlow()
                2 -> range2.asFlow()
                3 -> flow { throw DivideByZeroException() }
                else -> error("unexpected flowId $flowId")
            }
        }

        val flowService = flowService(flowFactory).proxy(Printer)

        val flow1 = flowService.createFlow(1)
        val flow2 = flowService.createFlow(2)
        val flow3 = flowService.createFlow(3)
        val flow4 = flowService.createFlow(4)

        assertFailsWith<IllegalStateException> { flow4.toList() }

        assertEquals(range1.toList(), flow1.toList())
        class CollectException : RuntimeException()
        assertSuspendFailsWith<CollectException> {
            flow1.collect { throw CollectException() }
        }

        assertSuspendFailsWith<DivideByZeroException> { flow3.toList() }

        coroutineScope {
            repeat(2) {
                launch {
                    assertEquals(range1.toList(), flow1.toList())
                }
                launch {
                    assertEquals(range2.toList(), flow2.toList())
                }
            }
        }
    }
}

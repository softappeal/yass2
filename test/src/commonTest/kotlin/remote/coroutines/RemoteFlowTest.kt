package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

private val Range0 = 1..2
private val Range1 = 1..10
private val Range2 = 2000..2020

private suspend fun test(flow0: Flow<Int>, flow1: Flow<Int>, flow2: Flow<Int>, flow3: Flow<Int>) {
    assertEquals(Range0.toList(), flow0.toList())

    assertEquals(Range1.toList(), flow1.toList())
    class CollectException : RuntimeException()
    assertSuspendFailsWith<CollectException> {
        flow1.collect { throw CollectException() }
    }

    assertSuspendFailsWith<DivideByZeroException> { flow3.toList() }

    val counter = AtomicInteger(0)
    coroutineScope {
        repeat(2) {
            launch {
                assertEquals(Range1.toList(), flow1.toList())
                counter.incrementAndGet()
            }
            launch {
                assertEquals(Range2.toList(), flow2.toList())
                counter.incrementAndGet()
            }
        }
    }
    assertEquals(5, counter.incrementAndGet())
}

private val TestFlowFactory = { flowId: Int ->
    when (flowId) {
        0 -> Range0.asFlow()
        1 -> Range1.asFlow()
        2 -> Range2.asFlow()
        3 -> flow { throw DivideByZeroException() }
        else -> error("unexpected flowId $flowId")
    }
}

suspend fun FlowService.test() {
    test(createFlow(0), createFlow(1), createFlow(2), createFlow(3))
}

@Suppress("UNCHECKED_CAST") val FlowServiceImpl = flowService(TestFlowFactory as FlowFactory)

class RemoteFlowTest {
    @Test
    fun noService() = runTest {
        test(
            TestFlowFactory(0),
            TestFlowFactory(1),
            TestFlowFactory(2),
            TestFlowFactory(3),
        )
        assertFailsMessage<Exception>("unexpected flowId 13") { TestFlowFactory(13) }
    }

    @Test
    fun withService() = runTest {
        GeneratedProxyFactory(FlowServiceImpl, Printer).test()
    }
}

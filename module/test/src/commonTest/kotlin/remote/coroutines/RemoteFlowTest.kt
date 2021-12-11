package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.*

private val Range0 = 1..2
private val Range1 = 1..100
private val Range2 = 2000..2200

private suspend fun test(flow0: Flow<Int>, flow1: Flow<Int>, flow2: Flow<Int>, flow3: Flow<Int>) {
    assertEquals(Range0.toList(), flow0.toList())

    class CollectException : RuntimeException()
    assertSuspendFailsWith<CollectException> {
        @OptIn(InternalCoroutinesApi::class) // TODO: remove
        flow1.collect { throw CollectException() }
    }

    assertSuspendFailsWith<DivideByZeroException> { flow3.collect() }

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

private val TestFlowFactory: FlowFactory = { flowId ->
    when (flowId) {
        0 -> Range0.asFlow()
        1 -> Range1.asFlow()
        2 -> Range2.asFlow()
        3 -> flow<Int> { throw DivideByZeroException() }
        else -> error("unexpected flowId $flowId")
    }
}

suspend fun FlowService.test() {
    test(createFlow(0), createFlow(1), createFlow(2), createFlow(3))
}

val FlowServiceImpl: FlowService = flowService(TestFlowFactory)

class RemoteFlowTest {
    @Test
    fun noService() = yassRunBlocking {
        @Suppress("UNCHECKED_CAST")
        test(
            TestFlowFactory(0) as Flow<Int>,
            TestFlowFactory(1) as Flow<Int>,
            TestFlowFactory(2) as Flow<Int>,
            TestFlowFactory(3) as Flow<Int>,
        )
    }

    @Test
    fun withService() = yassRunBlocking {
        FlowServiceImpl.test()
    }
}

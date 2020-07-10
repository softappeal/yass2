package ch.softappeal.yass2.remote

import ch.softappeal.yass2.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.*

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
        val counter = AtomicInteger(0)
        coroutineScope {
            repeat(2) {
                launch {
                    assertEquals(range1.toList(), flow1.toList())
                    counter.incrementAndGet()
                }
                launch {
                    assertEquals(range2.toList(), flow2.toList())
                    counter.incrementAndGet()
                }
            }
        }
        assertEquals(5, counter.incrementAndGet())
    }
}

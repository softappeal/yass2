package ch.softappeal.yass2.transport

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import kotlin.test.*

class TransportConfigTest {
    @Test
    fun testReadBytes() = yassRunBlocking {
        val config = TransportConfig(ContractSerializer, 50, 100)

        assertEquals(50, config.writer().buffer.size)

        class Step(val size: Int, val offset: Int, val length: Int)

        suspend fun check(length: Int, vararg steps: Step) {
            var counter = 0
            val buffer = config.readBytes(length) { bytes, offset, l ->
                val step = steps[counter++]
                assertEquals(step.size, bytes.size)
                assertEquals(step.offset, offset)
                assertEquals(step.length, l)
            }
            assertEquals(length, buffer.size)
            assertEquals(counter, steps.size)
        }

        check(10,
            Step(10, 0, 10)
        )
        check(100,
            Step(100, 0, 100)
        )
        check(101,
            Step(100, 0, 100),
            Step(101, 100, 1)
        )
        check(1000,
            Step(100, 0, 100),
            Step(200, 100, 100),
            Step(400, 200, 200),
            Step(800, 400, 400),
            Step(1000, 800, 200)
        )
    }
}

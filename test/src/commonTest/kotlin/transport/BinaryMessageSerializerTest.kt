package ch.softappeal.yass2.transport

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.*
import kotlin.test.*

private fun <T : Message> copy(value: T): T = MessageSerializer.copy(value)

class BinaryMessageSerializerTest {
    @Test
    fun request() {
        val serviceId = 123456
        val functionId = 4711
        val request = copy(Request(serviceId, functionId, listOf()))
        assertEquals(serviceId, request.serviceId)
        assertEquals(functionId, request.functionId)
        assertTrue(request.parameters.isEmpty())
    }

    @Test
    fun requestParameterGraph() {
        val serviceId = 123456
        val functionId = 4711
        val node = Node(123)
        val request = copy(Request(serviceId, functionId, listOf(node, node)))
        assertEquals(serviceId, request.serviceId)
        assertEquals(functionId, request.functionId)
        assertEquals(2, request.parameters.size)
        val node0 = request.parameters[0] as Node
        val node1 = request.parameters[1] as Node
        assertEquals(node.id, node0.id)
        assertSame(node0, node1)
    }

    @Test
    fun value() {
        val value = "xyz"
        val reply = copy(ValueReply(value))
        assertEquals(value, reply.value)
    }

    @Test
    fun exception() {
        val reply = copy(ExceptionReply(DivideByZeroException()))
        assertTrue(reply.exception is DivideByZeroException)
    }

    @Test
    fun writeNull() = assertFailsMessage<IllegalStateException>("unexpected value 'null'") {
        MessageSerializer.write(BytesWriter(100), null)
    }

    @Test
    fun writeString() = assertFailsMessage<IllegalStateException>("unexpected value 's'") {
        MessageSerializer.write(BytesWriter(100), "s")
    }

    @Test
    fun invalidType() = assertFailsMessage<IllegalStateException>("unexpected type 123") {
        MessageSerializer.read(BytesReader(byteArrayOf(123)))
    }
}

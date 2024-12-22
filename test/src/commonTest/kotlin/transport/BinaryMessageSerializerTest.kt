package ch.softappeal.yass2.transport

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.MessageSerializer
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Message
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.serialize.binary.copy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

package ch.softappeal.yass2.transport

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.*
import kotlin.test.*

fun <T> Serializer.copy(value: T): T {
    val writer = BytesWriter(1000)
    var size: Int
    with(writer) {
        write(this, value)
        size = current
    }
    return with(BytesReader(writer.buffer)) {
        @Suppress("UNCHECKED_CAST") val result = read(this) as T
        assertEquals(size, internalCurrent(this))
        result
    }
}

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

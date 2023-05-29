package ch.softappeal.yass2.transport.session

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import kotlin.test.*

private fun <T : Packet?> copy(value: T): T = PacketSerializer.copy(value)

class BinaryPacketSerializerTest {
    @Test
    fun nullValue() {
        assertNull(copy(null))
    }

    @Test
    fun packet() {
        val value = "v"
        val packet = Packet(123456789, ValueReply(value))
        val reply = copy(packet)
        assertEquals(packet.requestNumber, reply.requestNumber)
        assertEquals(value, (reply.message as ValueReply).value)
    }

    @Test
    fun writeString() = assertFailsMessage<IllegalStateException>("unexpected value 's'") {
        PacketSerializer.write(BytesWriter(100), "s")
    }
}

package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.PacketSerializer
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.binary.copy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

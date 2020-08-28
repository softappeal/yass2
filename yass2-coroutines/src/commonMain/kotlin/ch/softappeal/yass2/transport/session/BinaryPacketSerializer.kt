package ch.softappeal.yass2.transport.session

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*

public fun binaryPacketSerializer(messageSerializer: Serializer): Serializer = object : Serializer {
    override fun write(writer: Writer, value: Any?) {
        if (value == null) {
            writer.writeBoolean(false)
        } else {
            writer.writeBoolean(true)
            val packet = value as Packet
            writer.writeInt(packet.requestNumber)
            messageSerializer.write(writer, packet.message)
        }
    }

    override fun read(reader: Reader): Packet? =
        if (reader.readBoolean()) Packet(reader.readInt(), messageSerializer.read(reader) as Message) else null
}

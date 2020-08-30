package ch.softappeal.yass2.transport.session

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*

public fun binaryPacketSerializer(messageSerializer: Serializer): Serializer = object : Serializer {
    override fun write(writer: Writer, value: Any?) = when (value) {
        null -> writer.writeBoolean(false)
        is Packet -> {
            writer.writeBoolean(true)
            writer.writeInt(value.requestNumber)
            messageSerializer.write(writer, value.message)
        }
        else -> error("unexpected value '$value'")
    }

    override fun read(reader: Reader): Packet? =
        if (reader.readBoolean()) Packet(reader.readInt(), messageSerializer.read(reader) as Message) else null
}

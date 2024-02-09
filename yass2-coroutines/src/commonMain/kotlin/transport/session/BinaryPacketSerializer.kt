package ch.softappeal.yass2.transport.session

import ch.softappeal.yass2.remote.Message
import ch.softappeal.yass2.remote.coroutines.session.Packet
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.binary.readBoolean
import ch.softappeal.yass2.serialize.binary.readInt
import ch.softappeal.yass2.serialize.binary.writeBoolean
import ch.softappeal.yass2.serialize.binary.writeInt

/**
 * Returns a binary [Serializer] for [Packet]?.
 * [messageSerializer] must be able to serialize [Message].
 */
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

package ch.softappeal.yass2.remote.coroutines

import ch.softappeal.yass2.remote.Message
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.binary.readBinaryBoolean
import ch.softappeal.yass2.serialize.binary.readBinaryInt
import ch.softappeal.yass2.serialize.binary.writeBinaryBoolean
import ch.softappeal.yass2.serialize.binary.writeBinaryInt

/**
 * Returns a binary [Serializer] for [Packet]?.
 * [messageSerializer] must be able to serialize the used [Message].
 */
public fun binaryPacketSerializer(messageSerializer: Serializer): Serializer = object : Serializer {
    override fun write(writer: Writer, value: Any?) = when (value) {
        null -> writer.writeBinaryBoolean(false)
        is Packet -> {
            writer.writeBinaryBoolean(true)
            writer.writeBinaryInt(value.requestNumber)
            messageSerializer.write(writer, value.message)
        }
        else -> error("unexpected value '$value'")
    }

    override fun read(reader: Reader): Packet? =
        if (reader.readBinaryBoolean()) Packet(reader.readBinaryInt(), messageSerializer.read(reader) as Message) else null
}

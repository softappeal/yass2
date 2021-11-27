package ch.softappeal.yass2.transport

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*

private const val RequestType = 0.toByte()
private const val ValueReplyType = 1.toByte()
private const val ExceptionReplyType = 2.toByte()

/**
 * Returns a binary [Serializer] for [Message].
 * [contractSerializer] must be able to serialize the used contract.
 */
public fun binaryMessageSerializer(contractSerializer: Serializer): Serializer = object : Serializer {
    override fun write(writer: Writer, value: Any?) = when (value) {
        is Request -> {
            writer.writeByte(RequestType)
            writer.writeVarInt(value.serviceId)
            writer.writeVarInt(value.functionId)
            writer.writeByte(value.parameters.size.toByte())
            value.parameters.forEach { contractSerializer.write(writer, it) }
        }
        is ValueReply -> {
            writer.writeByte(ValueReplyType)
            contractSerializer.write(writer, value.value)
        }
        is ExceptionReply -> {
            writer.writeByte(ExceptionReplyType)
            contractSerializer.write(writer, value.exception)
        }
        else -> error("unexpected value '$value'")
    }

    private fun readList(reader: Reader): List<Any?> {
        var size = reader.readByte().toInt()
        val list = ArrayList<Any?>(size)
        while (size-- > 0) list.add(contractSerializer.read(reader))
        return list
    }

    override fun read(reader: Reader): Message = when (val type = reader.readByte()) {
        RequestType -> Request(
            reader.readVarInt(),
            reader.readVarInt(),
            readList(reader),
        )
        ValueReplyType -> ValueReply(
            contractSerializer.read(reader)
        )
        ExceptionReplyType -> ExceptionReply(
            contractSerializer.read(reader) as Exception
        )
        else -> error("unexpected type $type")
    }
}

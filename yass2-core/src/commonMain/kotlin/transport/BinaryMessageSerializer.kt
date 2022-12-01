package ch.softappeal.yass2.transport

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*

private const val REQUEST_TYPE = 0.toByte()
private const val VALUE_REPLY_TYPE = 1.toByte()
private const val EXCEPTION_REPLY_TYPE = 2.toByte()

/**
 * Returns a binary [Serializer] for [Message].
 * [contractSerializer] must be able to serialize [List] and the used contract.
 */
public fun binaryMessageSerializer(contractSerializer: Serializer): Serializer = object : Serializer {
    override fun write(writer: Writer, value: Any?) = when (value) {
        is Request -> {
            writer.writeByte(REQUEST_TYPE)
            writer.writeVarInt(value.serviceId)
            writer.writeVarInt(value.functionId)
            contractSerializer.write(writer, value.parameters)
        }
        is ValueReply -> {
            writer.writeByte(VALUE_REPLY_TYPE)
            contractSerializer.write(writer, value.value)
        }
        is ExceptionReply -> {
            writer.writeByte(EXCEPTION_REPLY_TYPE)
            contractSerializer.write(writer, value.exception)
        }
        else -> error("unexpected value '$value'")
    }

    override fun read(reader: Reader): Message = when (val type = reader.readByte()) {
        REQUEST_TYPE -> Request(
            reader.readVarInt(),
            reader.readVarInt(),
            contractSerializer.read(reader) as List<Any?>,
        )
        VALUE_REPLY_TYPE -> ValueReply(
            contractSerializer.read(reader),
        )
        EXCEPTION_REPLY_TYPE -> ExceptionReply(
            contractSerializer.read(reader) as Exception,
        )
        else -> error("unexpected type $type")
    }
}

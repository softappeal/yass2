package ch.softappeal.yass2.transport

import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Message
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.binary.readVarInt
import ch.softappeal.yass2.serialize.binary.writeVarInt

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

package ch.softappeal.yass2.remote

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
 * [contractSerializer] must be able to serialize the used contract.
 */
public fun binaryMessageSerializer(contractSerializer: Serializer): Serializer = object : Serializer {
    override fun write(writer: Writer, value: Any?) = when (value) {
        is Request -> {
            writer.writeByte(REQUEST_TYPE)
            writer.writeVarInt(value.serviceId)
            writer.writeVarInt(value.functionId)
            writer.writeVarInt(value.parameters.size)
            value.parameters.forEach { contractSerializer.write(writer, it) }
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

    fun Reader.readParameters(): List<Any?> {
        var size = readVarInt()
        return ArrayList<Any?>(minOf(size, 5)).apply { // prevents easy out-of-memory attack
            while (size-- > 0) add(contractSerializer.read(this@readParameters))
        }
    }

    override fun read(reader: Reader): Message = when (val type = reader.readByte()) {
        REQUEST_TYPE -> Request(
            reader.readVarInt(),
            reader.readVarInt(),
            reader.readParameters(),
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

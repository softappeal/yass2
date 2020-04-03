package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.*
import kotlin.reflect.*

public val BooleanEncoder: BaseEncoder<Boolean> = BaseEncoder(Boolean::class,
    { writer, value -> writer.writeBoolean(value) },
    { reader -> reader.readBoolean() }
)

public val ByteEncoder: BaseEncoder<Byte> = BaseEncoder(Byte::class,
    { writer, value -> writer.writeByte(value) },
    { reader -> reader.readByte() }
)

public val IntEncoder: BaseEncoder<Int> = BaseEncoder(Int::class,
    { writer, value -> writer.writeVarInt(value.toZigZag()) },
    { reader -> reader.readVarInt().fromZigZag() }
)

public val LongEncoder: BaseEncoder<Long> = BaseEncoder(Long::class,
    { writer, value -> writer.writeVarLong(value.toZigZag()) },
    { reader -> reader.readVarLong().fromZigZag() }
)

public val DoubleEncoder: BaseEncoder<Double> = BaseEncoder(Double::class,
    { writer, value -> writer.writeLong(value.toBits()) },
    { reader -> Double.fromBits(reader.readLong()) }
)

public val ByteArrayEncoder: BaseEncoder<ByteArray> = BaseEncoder(ByteArray::class,
    { writer, value ->
        writer.writeVarInt(value.size)
        writer.writeBytes(value)
    },
    { reader -> reader.readBytes(reader.readVarInt()) }
)

public val StringEncoder: BaseEncoder<String> = BaseEncoder(String::class,
    { writer, value -> ByteArrayEncoder.write(writer, value.encodeToByteArray(throwOnInvalidSequence = true)) },
    { reader -> ByteArrayEncoder.read(reader).decodeToString(throwOnInvalidSequence = true) }
)

@PublishedApi
internal fun <T : Enum<T>> enumEncoder(type: KClass<T>, constants: Array<T>) = BaseEncoder(type,
    { writer, value -> writer.writeVarInt(value.ordinal) },
    { reader -> constants[reader.readVarInt()] }
)

public inline fun <reified T : Enum<T>> enumEncoder(): BaseEncoder<T> = enumEncoder(T::class, enumValues())

public fun <T : Any> BaseEncoder<T>.writeOptional(writer: Writer, value: T?): Unit = if (value == null) {
    writer.writeBoolean(false)
} else {
    writer.writeBoolean(true)
    write(writer, value)
}

public fun <T : Any> BaseEncoder<T>.readOptional(reader: Reader): T? = if (reader.readBoolean()) read(reader) else null

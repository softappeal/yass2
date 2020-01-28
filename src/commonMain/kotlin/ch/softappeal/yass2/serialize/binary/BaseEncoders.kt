package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.*
import kotlin.reflect.*

val BooleanEncoder = BaseEncoder(Boolean::class,
    { writer, value -> writer.writeBoolean(value) },
    { reader -> reader.readBoolean() }
)

val ByteEncoder = BaseEncoder(Byte::class,
    { writer, value -> writer.writeByte(value) },
    { reader -> reader.readByte() }
)

val IntEncoder = BaseEncoder(Int::class,
    { writer, value -> writer.writeVarInt(value.toZigZag()) },
    { reader -> reader.readVarInt().fromZigZag() }
)

val LongEncoder = BaseEncoder(Long::class,
    { writer, value -> writer.writeVarLong(value.toZigZag()) },
    { reader -> reader.readVarLong().fromZigZag() }
)

val DoubleEncoder = BaseEncoder(Double::class,
    { writer, value -> writer.writeLong(value.toBits()) },
    { reader -> Double.fromBits(reader.readLong()) }
)

val ByteArrayEncoder = BaseEncoder(ByteArray::class,
    { writer, value ->
        writer.writeVarInt(value.size)
        writer.writeBytes(value)
    },
    { reader -> reader.readBytes(reader.readVarInt()) }
)

@UseExperimental(ExperimentalStdlibApi::class) // TODO
val StringEncoder = BaseEncoder(String::class,
    { writer, value -> ByteArrayEncoder.write(writer, value.encodeToByteArray(throwOnInvalidSequence = true)) },
    { reader -> ByteArrayEncoder.read(reader).decodeToString(throwOnInvalidSequence = true) }
)

@PublishedApi
internal fun <T : Enum<T>> enumEncoder(type: KClass<T>, constants: Array<T>) = BaseEncoder(type,
    { writer, value -> writer.writeVarInt(value.ordinal) },
    { reader -> constants[reader.readVarInt()] }
)

inline fun <reified T : Enum<T>> enumEncoder() = enumEncoder(T::class, enumValues())

fun <T : Any> BaseEncoder<T>.writeOptional(writer: Writer, value: T?): Unit = if (value == null) {
    writer.writeBoolean(false)
} else {
    writer.writeBoolean(true)
    write(writer, value)
}

fun <T : Any> BaseEncoder<T>.readOptional(reader: Reader): T? = if (reader.readBoolean()) read(reader) else null

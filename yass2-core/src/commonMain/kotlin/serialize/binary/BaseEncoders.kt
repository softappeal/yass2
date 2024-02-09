package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer
import kotlin.reflect.KClass

public fun Writer.writeZigZagVarInt(value: Int) {
    writeVarInt(value.toZigZag())
}

public fun Reader.readZigZagVarInt(): Int = readVarInt().fromZigZag()

public fun Writer.writeZigZagVarLong(value: Long) {
    writeVarLong(value.toZigZag())
}

public fun Reader.readZigZagVarLong(): Long = readVarLong().fromZigZag()

public fun Writer.writeDouble(value: Double) {
    writeLong(value.toBits())
}

public fun Reader.readDouble(): Double = Double.fromBits(readLong())

public fun Writer.writeByteArray(value: ByteArray) {
    writeVarInt(value.size)
    writeBytes(value)
}

public fun Reader.readByteArray(): ByteArray = readBytes(readVarInt())

public fun Writer.writeString(value: String) {
    writeByteArray(value.encodeToByteArray(throwOnInvalidSequence = true))
}

public fun Reader.readString(): String = readByteArray().decodeToString(throwOnInvalidSequence = true)

public class BooleanEncoder : BaseEncoder<Boolean>(Boolean::class,
    { writer, value -> writer.writeBoolean(value) },
    { reader -> reader.readBoolean() }
)

public class ByteEncoder : BaseEncoder<Byte>(Byte::class,
    { writer, value -> writer.writeByte(value) },
    { reader -> reader.readByte() }
)

public class IntEncoder : BaseEncoder<Int>(Int::class,
    { writer, value -> writer.writeZigZagVarInt(value) },
    { reader -> reader.readZigZagVarInt() }
)

public class LongEncoder : BaseEncoder<Long>(Long::class,
    { writer, value -> writer.writeZigZagVarLong(value) },
    { reader -> reader.readZigZagVarLong() }
)

public class DoubleEncoder : BaseEncoder<Double>(Double::class,
    { writer, value -> writer.writeDouble(value) },
    { reader -> reader.readDouble() }
)

public class ByteArrayEncoder : BaseEncoder<ByteArray>(ByteArray::class,
    { writer, value -> writer.writeByteArray(value) },
    { reader -> reader.readByteArray() }
)

public class StringEncoder : BaseEncoder<String>(String::class,
    { writer, value -> writer.writeString(value) },
    { reader -> reader.readString() }
)

public abstract class EnumEncoder<T : Enum<T>>(type: KClass<T>, constants: Array<T>) : BaseEncoder<T>(type,
    { writer, value -> writer.writeVarInt(value.ordinal) },
    { reader -> constants[reader.readVarInt()] }
)

public fun <T : Any> Writer.writeOptional(value: T?, write: Writer.(value: T) -> Unit): Unit = if (value == null) {
    writeBoolean(false)
} else {
    writeBoolean(true)
    write(value)
}

public fun <T : Any> Reader.readOptional(read: Reader.() -> T): T? = if (readBoolean()) read() else null

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

public class BooleanEncoder : Encoder<Boolean>(Boolean::class,
    { value -> writeBoolean(value) },
    { readBoolean() }
)

public class ByteEncoder : Encoder<Byte>(Byte::class,
    { value -> writeByte(value) },
    { readByte() }
)

public class IntEncoder : Encoder<Int>(Int::class,
    { value -> writeZigZagVarInt(value) },
    { readZigZagVarInt() }
)

public class LongEncoder : Encoder<Long>(Long::class,
    { value -> writeZigZagVarLong(value) },
    { readZigZagVarLong() }
)

public class DoubleEncoder : Encoder<Double>(Double::class,
    { value -> writeDouble(value) },
    { readDouble() }
)

public class ByteArrayEncoder : Encoder<ByteArray>(ByteArray::class,
    { value -> writeByteArray(value) },
    { readByteArray() }
)

public class StringEncoder : Encoder<String>(String::class,
    { value -> writeString(value) },
    { readString() }
)

public abstract class EnumEncoder<T : Enum<T>>(type: KClass<T>, constants: Array<T>) : Encoder<T>(type,
    { value -> writeVarInt(value.ordinal) },
    { constants[readVarInt()] }
)

public fun <T : Any> Writer.writeOptional(value: T?, write: Writer.(value: T) -> Unit): Unit = if (value == null) {
    writeBoolean(false)
} else {
    writeBoolean(true)
    write(value)
}

public fun <T : Any> Reader.readOptional(read: Reader.() -> T): T? = if (readBoolean()) read() else null

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

public fun Writer.writeBinaryDouble(value: Double) {
    writeBinaryLong(value.toBits())
}

public fun Reader.readBinaryDouble(): Double = Double.fromBits(readBinaryLong())

public fun Writer.writeBinaryByteArray(value: ByteArray) {
    writeVarInt(value.size)
    writeBytes(value)
}

public fun Reader.readBinaryByteArray(): ByteArray = readBytes(readVarInt())

public fun Writer.writeBinaryString(value: String) {
    writeBinaryByteArray(value.encodeToByteArray(throwOnInvalidSequence = true))
}

public fun Reader.readBinaryString(): String = readBinaryByteArray().decodeToString(throwOnInvalidSequence = true)

public class BooleanBinaryEncoder : BinaryEncoder<Boolean>(Boolean::class,
    { value -> writeBinaryBoolean(value) },
    { readBinaryBoolean() }
)

public class ByteBinaryEncoder : BinaryEncoder<Byte>(Byte::class,
    { value -> writeByte(value) },
    { readByte() }
)

public class IntBinaryEncoder : BinaryEncoder<Int>(Int::class,
    { value -> writeZigZagVarInt(value) },
    { readZigZagVarInt() }
)

public class LongBinaryEncoder : BinaryEncoder<Long>(Long::class,
    { value -> writeZigZagVarLong(value) },
    { readZigZagVarLong() }
)

public class DoubleBinaryEncoder : BinaryEncoder<Double>(Double::class,
    { value -> writeBinaryDouble(value) },
    { readBinaryDouble() }
)

public class ByteArrayBinaryEncoder : BinaryEncoder<ByteArray>(ByteArray::class,
    { value -> writeBinaryByteArray(value) },
    { readBinaryByteArray() }
)

public class StringBinaryEncoder : BinaryEncoder<String>(String::class,
    { value -> writeBinaryString(value) },
    { readBinaryString() }
)

public class EnumBinaryEncoder<T : Enum<T>>(type: KClass<T>, constants: Array<T>) : BinaryEncoder<T>(type,
    { value -> writeVarInt(value.ordinal) },
    { constants[readVarInt()] }
)

public fun <T : Any> Writer.writeBinaryOptional(value: T?, write: Writer.(value: T) -> Unit): Unit = if (value == null) {
    writeBinaryBoolean(false)
} else {
    writeBinaryBoolean(true)
    write(value)
}

public fun <T : Any> Reader.readBinaryOptional(read: Reader.() -> T): T? = if (readBinaryBoolean()) read() else null

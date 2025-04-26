package ch.softappeal.yass2.core.serialize.binary

import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer
import kotlin.reflect.KClass

public object BooleanBinaryEncoder : BinaryEncoder<Boolean>(
    Boolean::class,
    { value -> writeBinaryBoolean(value) },
    { readBinaryBoolean() }
)

public object IntBinaryEncoder : BinaryEncoder<Int>(
    Int::class,
    { value -> writeVarInt(value.toZigZag()) },
    { readVarInt().fromZigZag() }
)

public object LongBinaryEncoder : BinaryEncoder<Long>(
    Long::class,
    { value -> writeVarLong(value.toZigZag()) },
    { readVarLong().fromZigZag() }
)

public object DoubleBinaryEncoder : BinaryEncoder<Double>(
    Double::class,
    { value -> writeBinaryLong(value.toBits()) },
    { Double.fromBits(readBinaryLong()) }
)

public object BytesBinaryEncoder : BinaryEncoder<ByteArray>(
    ByteArray::class,
    { value ->
        writeVarInt(value.size)
        writeBytes(value)
    },
    { readBytes(readVarInt()) }
)

public object StringBinaryEncoder : BinaryEncoder<String>(
    String::class,
    { value -> BytesBinaryEncoder.write(this, value.encodeToByteArray(throwOnInvalidSequence = true)) },
    { BytesBinaryEncoder.read(this).decodeToString(throwOnInvalidSequence = true) }
)

public class EnumBinaryEncoder<T : Enum<T>>(type: KClass<T>, constants: Array<T>) : BinaryEncoder<T>(
    type,
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

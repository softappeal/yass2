package ch.softappeal.yass2.core.serialize.binary

import kotlin.enums.EnumEntries
import kotlin.reflect.KClass

public object BooleanBinaryEncoder : BinaryEncoder<Boolean>(
    Boolean::class,
    { value -> writeBinaryBoolean(value) },
    { readBinaryBoolean() },
)

public object IntBinaryEncoder : BinaryEncoder<Int>(
    Int::class,
    { value -> writeVarInt(value.toZigZag()) },
    { readVarInt().fromZigZag() },
)

public object LongBinaryEncoder : BinaryEncoder<Long>(
    Long::class,
    { value -> writeVarLong(value.toZigZag()) },
    { readVarLong().fromZigZag() }
)

public object ByteArrayBinaryEncoder : BinaryEncoder<ByteArray>(
    ByteArray::class,
    { value ->
        writeVarInt(value.size)
        writeByteArray(value)
    },
    { readByteArray(readVarInt()) },
)

public object StringBinaryEncoder : BinaryEncoder<String>(
    String::class,
    { value -> ByteArrayBinaryEncoder.write(this, value.encodeToByteArray(throwOnInvalidSequence = true)) },
    { ByteArrayBinaryEncoder.read(this).decodeToString(throwOnInvalidSequence = true) },
)

public class EnumBinaryEncoder<T : Enum<T>>(type: KClass<T>, enumEntries: EnumEntries<T>) : BinaryEncoder<T>(
    type,
    { value -> writeVarInt(value.ordinal) },
    {
        val c = readVarInt()
        check(c in enumEntries.indices) { "illegal constant $c" }
        enumEntries[c]
    },
)

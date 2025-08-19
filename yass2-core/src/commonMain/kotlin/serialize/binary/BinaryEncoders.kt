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

public object ByteArrayBinaryEncoder : BinaryEncoder<ByteArray>(
    ByteArray::class,
    { value ->
        writeVarInt(value.size)
        writeByteArray(value)
    },
    { readByteArray(readVarInt()) }
)

public object StringBinaryEncoder : BinaryEncoder<String>(
    String::class,
    { value -> ByteArrayBinaryEncoder.write(this, value.encodeToByteArray(throwOnInvalidSequence = true)) },
    { ByteArrayBinaryEncoder.read(this).decodeToString(throwOnInvalidSequence = true) }
)

public class EnumBinaryEncoder<T : Enum<T>>(type: KClass<T>, enumValues: Array<T>) : BinaryEncoder<T>(
    type,
    { value -> writeVarInt(value.ordinal) },
    {
        val c = readVarInt()
        check(c in 0..<enumValues.size) { "illegal constant $c" }
        enumValues[c]
    }
)

public fun <T : Any> Writer.writeBinaryOptional(value: T?, write: Writer.(value: T) -> Unit): Unit = if (value == null) {
    writeBinaryBoolean(false)
} else {
    writeBinaryBoolean(true)
    write(value)
}

public fun <T : Any> Reader.readBinaryOptional(read: Reader.() -> T): T? = if (readBinaryBoolean()) read() else null

package ch.softappeal.yass2.serialize

import ch.softappeal.yass2.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.text.TextEncoder
import ch.softappeal.yass2.serialize.text.TextSerializer
import kotlin.reflect.KClass

public interface Writer {
    public fun writeByte(byte: Byte)
    public fun writeBytes(bytes: ByteArray)
}

public interface Reader {
    public fun readByte(): Byte
    public fun readBytes(length: Int): ByteArray
}

public interface Serializer {
    public fun write(writer: Writer, value: Any?)
    public fun read(reader: Reader): Any?
}

/**
 * [concreteClasses] must be concrete and must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 * [Enum] classes also belong to [concreteClasses].
 * Generates a [BinarySerializer] if [binaryEncoderClasses] isn't empty.
 * Generates a [TextSerializer] (with built-in encoder for [String]) if [textEncoderClasses] isn't empty.
 */
@Target(AnnotationTarget.PROPERTY)
public annotation class GenerateSerializer(
    val concreteClasses: Array<KClass<*>>,
    val binaryEncoderClasses: Array<KClass<out BinaryEncoder<*>>>,
    val textEncoderClasses: Array<KClass<out TextEncoder<*>>>,
)

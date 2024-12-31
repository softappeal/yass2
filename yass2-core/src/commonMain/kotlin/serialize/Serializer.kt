package ch.softappeal.yass2.serialize

import ch.softappeal.yass2.serialize.binary.BinaryEncoder
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
 */
@Target(AnnotationTarget.PROPERTY)
public annotation class GenerateSerializer(
    val binaryEncoderClasses: Array<KClass<out BinaryEncoder<*>>>,
    val concreteClasses: Array<KClass<*>>,
)

package ch.softappeal.yass2.serialize

import kotlin.reflect.KClass

public interface Writer {
    public fun writeByte(byte: Byte)
    public fun writeByteArray(byteArray: ByteArray)
}

public interface Reader {
    public fun readByte(): Byte
    public fun readByteArray(length: Int): ByteArray
}

public interface Serializer {
    public fun write(writer: Writer, value: Any?)
    public fun read(reader: Reader): Any?
}

public fun Serializer.toByteArray(value: Any?): ByteArray = with(ByteArrayWriter()) {
    write(this, value)
    toyByteArray()
}

public fun Serializer.fromByteArray(byteArray: ByteArray): Any? = with(ByteArrayReader(byteArray)) {
    read(this).apply { checkDrained() }
}

/**
 * Concrete classes must have a primary constructor and all its parameters must be properties.
 * Properties can be optional.
 * Inheritance is supported.
 * Exceptions are supported but [Throwable.message] and [Throwable.cause] aren't serialized.
 */
public annotation class ConcreteAndEnumClasses(vararg val value: KClass<*>)

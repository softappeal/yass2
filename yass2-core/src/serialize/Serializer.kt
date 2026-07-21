package ch.softappeal.yass2.core.serialize

public interface Writer {
    public fun writeByte(byte: Byte)
    public fun writeByteArray(byteArray: ByteArray)
}

public interface Reader {
    public fun readByte(): Byte
    public fun readByteArray(length: Int): ByteArray
}

/**
 * Concrete classes must have a primary constructor and all its parameters must be properties.
 * Properties can be optional.
 * Inheritance is supported.
 * Exceptions are supported but [Throwable.message] and [Throwable.cause] aren't serialized.
 */
public interface Serializer {
    public fun Writer.write(value: Any?)
    public fun Reader.read(): Any?
}

public fun Serializer.write(writer: Writer, value: Any?) {
    writer.write(value)
}

public fun Serializer.read(reader: Reader): Any? = reader.read()

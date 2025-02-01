package ch.softappeal.yass2.serialize

import kotlin.reflect.KProperty1

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

public fun Serializer.writeBytes(value: Any?): ByteArray = with(BytesWriter(1000)) {
    write(this, value)
    buffer.copyOf(current)
}

public fun Serializer.readBytes(byteArray: ByteArray): Any? = with(BytesReader(byteArray)) {
    read(this).apply { checkDrained() }
}

public abstract class Property(public val property: KProperty1<out Any, *>)

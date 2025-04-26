package ch.softappeal.yass2.core.serialize

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

public fun Serializer.toByteArray(value: Any?): ByteArray = with(ByteArrayWriter(1000)) {
    write(this, value)
    toyByteArray()
}

public fun Serializer.fromByteArray(byteArray: ByteArray): Any? = with(ByteArrayReader(byteArray)) {
    read(this).apply { checkDrained() }
}

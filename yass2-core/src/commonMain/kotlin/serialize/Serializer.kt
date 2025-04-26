package ch.softappeal.yass2.core.serialize

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

public fun Serializer.toBytes(value: Any?): ByteArray = with(BytesWriter(1000)) {
    write(this, value)
    toyBytes()
}

public fun Serializer.fromBytes(bytes: ByteArray): Any? = with(BytesReader(bytes)) {
    read(this).apply { checkDrained() }
}

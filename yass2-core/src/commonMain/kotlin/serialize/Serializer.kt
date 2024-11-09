package ch.softappeal.yass2.serialize

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
 * Should only be used to bootstrap code generation.
 */
public val DummySerializer: Serializer = object : Serializer {
    override fun write(writer: Writer, value: Any?) {
        error("DummySerializer")
    }

    override fun read(reader: Reader): Any? {
        error("DummySerializer")
    }
}

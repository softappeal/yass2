package ch.softappeal.yass2.serialize

interface Writer {
    fun writeByte(byte: Byte)
    fun writeBytes(bytes: ByteArray)
}

interface Reader {
    fun readByte(): Byte
    fun readBytes(length: Int): ByteArray
}

interface Serializer {
    fun write(writer: Writer, value: Any?)
    fun read(reader: Reader): Any?
}

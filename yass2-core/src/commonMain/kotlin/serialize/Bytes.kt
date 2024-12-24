package ch.softappeal.yass2.serialize

public class BytesWriter(public var buffer: ByteArray) : Writer {
    public constructor(initialCapacity: Int) : this(ByteArray(initialCapacity))

    public var current: Int = 0

    override fun writeByte(byte: Byte) {
        if (current >= buffer.size) buffer = buffer.copyOf(maxOf(1000, 2 * buffer.size))
        buffer[current++] = byte
    }

    override fun writeBytes(bytes: ByteArray) {
        val newCurrent = current + bytes.size
        if (newCurrent > buffer.size) buffer = buffer.copyOf(maxOf(newCurrent + 1000, 2 * buffer.size))
        bytes.copyInto(buffer, current)
        current = newCurrent
    }
}

public class BytesReader(public var buffer: ByteArray) : Reader {
    public var current: Int = 0

    public val isDrained: Boolean get() = current >= buffer.size

    override fun readByte(): Byte {
        require(current < buffer.size)
        return buffer[current++]
    }

    override fun readBytes(length: Int): ByteArray {
        val newCurrent = current + length
        require(newCurrent <= buffer.size) // prevents allocation of ByteArray below if buffer is too small
        return ByteArray(length).apply {
            buffer.copyInto(this, 0, current, newCurrent)
            current = newCurrent
        }
    }
}

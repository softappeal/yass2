package ch.softappeal.yass2.transport

import ch.softappeal.yass2.serialize.*

public class BytesWriter(initialCapacity: Int) : Writer {
    public var buffer: ByteArray = ByteArray(initialCapacity)
        private set

    public var current: Int = 0
        private set

    override fun writeByte(byte: Byte) {
        if (current >= buffer.size) buffer = buffer.copyOf(maxOf(1000, 2 * buffer.size))
        buffer[current++] = byte
    }

    override fun writeBytes(bytes: ByteArray) {
        val newCurrent = current + bytes.size
        if (newCurrent > buffer.size) buffer = buffer.copyOf(maxOf(newCurrent, 2 * buffer.size))
        bytes.copyInto(buffer, current)
        current = newCurrent
    }
}

public class BytesReader(private val buffer: ByteArray) : Reader {
    internal var current: Int = 0
        private set

    public val isDrained: Boolean get() = current >= buffer.size

    override fun readByte(): Byte {
        require(current < buffer.size)
        return buffer[current++]
    }

    override fun readBytes(length: Int): ByteArray {
        val newCurrent = current + length
        require(newCurrent <= buffer.size)
        return ByteArray(length).apply {
            buffer.copyInto(this, 0, current, newCurrent)
            current = newCurrent
        }
    }
}

public fun internalCurrent(reader: BytesReader): Int = reader.current

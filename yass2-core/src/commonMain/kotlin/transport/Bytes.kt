package ch.softappeal.yass2.transport

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

public class BytesWriter(public var buffer: ByteArray) : Writer {
    public constructor(size: Int) : this(ByteArray(size))

    public var current: Int = 0

    override fun writeByte(byte: Byte) {
        buffer[current] = byte
        current += 1
    }

    override fun writeBytes(bytes: ByteArray) {
        val newCurrent = current + bytes.size
        bytes.copyInto(buffer, current)
        current = newCurrent
    }
}

public class BytesReader(public var buffer: ByteArray) : Reader {
    public var current: Int = 0

    public val isDrained: Boolean get() = current >= buffer.size

    override fun readByte(): Byte {
        val b = buffer[current]
        current += 1
        return b
    }

    override fun readBytes(length: Int): ByteArray {
        val newCurrent = current + length
        return ByteArray(length).apply {
            buffer.copyInto(this, 0, current, newCurrent)
            current = newCurrent
        }
    }
}

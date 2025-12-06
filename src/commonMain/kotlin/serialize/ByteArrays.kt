package ch.softappeal.yass2.serialize


public class ByteArrayWriter(initialCapacity: Int = 1000) : Writer {
    internal var byteArray: ByteArray = ByteArray(initialCapacity)
    private var current = 0

    override fun writeByte(byte: Byte) {
        if (current >= byteArray.size) byteArray = byteArray.copyOf(maxOf(1000, 2 * byteArray.size))
        byteArray[current++] = byte
    }

    override fun writeByteArray(byteArray: ByteArray) {
        val newCurrent = current + byteArray.size
        if (newCurrent > this.byteArray.size) {
            this.byteArray = this.byteArray.copyOf(maxOf(newCurrent + 1000, 2 * this.byteArray.size))
        }
        byteArray.copyInto(this.byteArray, current)
        current = newCurrent
    }

    public fun toyByteArray(): ByteArray = byteArray.copyOf(current)
}

public class ByteArrayReader(private val byteArray: ByteArray) : Reader {
    private var current = 0

    public val isDrained: Boolean get() = current >= byteArray.size

    override fun readByte(): Byte {
        require(current < byteArray.size) { "'readByte()' called when buffer is empty" }
        return byteArray[current++]
    }

    override fun readByteArray(length: Int): ByteArray {
        val newCurrent = current + length
        require(newCurrent <= byteArray.size) { "'readByteArray($length)' called when buffer is empty" }
        return ByteArray(length).apply {
            byteArray.copyInto(this, 0, current, newCurrent)
            current = newCurrent
        }
    }
}

public fun ByteArrayReader.checkDrained() {
    check(isDrained) { "buffer not drained" }
}

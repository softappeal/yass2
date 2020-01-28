package ch.softappeal.yass2.transport

import ch.softappeal.yass2.serialize.*

class TransportConfig(
    serializer: Serializer, private val initialWriterCapacity: Int, private val maxReadBytesInitialSize: Int = 1
) : Serializer by serializer {
    init {
        require(initialWriterCapacity > 0)
        require(maxReadBytesInitialSize > 0)
    }

    fun writer() = BytesWriter(initialWriterCapacity)

    suspend fun readBytes(length: Int, readBytes: suspend (bytes: ByteArray, offset: Int, length: Int) -> Unit): ByteArray {
        var buffer = ByteArray(minOf(length, maxReadBytesInitialSize))
        var current = 0
        while (current < length) { // prevents easy out-of-memory attack
            if (current >= buffer.size) buffer = buffer.copyOf(minOf(length, 2 * buffer.size))
            readBytes(buffer, current, buffer.size - current)
            current = buffer.size
        }
        return buffer
    }
}

package ch.softappeal.yass2.transport

import ch.softappeal.yass2.serialize.*

public class TransportConfig(
    serializer: Serializer, private val initialWriterCapacity: Int, private val maxReadBytesInitialSize: Int = 500
) : Serializer by serializer {
    init {
        require(initialWriterCapacity > 0)
        require(maxReadBytesInitialSize > 0)
    }

    public fun writer(): BytesWriter = BytesWriter(initialWriterCapacity)

    public suspend fun readBytes(
        length: Int, readBytes: suspend (bytes: ByteArray, offset: Int, length: Int) -> Unit
    ): ByteArray {
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

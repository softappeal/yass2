package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.core.serialize.ByteArrayWriter
import ch.softappeal.yass2.core.serialize.Serializer

public class Transport(
    serializer: Serializer,
    private val initialWriterCapacity: Int = 1000,
    private val maxReadInitialSize: Int = 1000,
) : Serializer by serializer {
    init {
        require(initialWriterCapacity > 0)
        require(maxReadInitialSize > 0)
    }

    internal fun createWriter() = ByteArrayWriter(initialWriterCapacity)

    internal suspend fun readByteArray(
        length: Int, readBytes: suspend (byteArray: ByteArray, offset: Int, length: Int) -> Unit,
    ): ByteArray {
        var byteArray = ByteArray(minOf(length, maxReadInitialSize))
        var current = 0
        while (current < length) { // prevents easy out-of-memory attack
            if (current >= byteArray.size) byteArray = byteArray.copyOf(minOf(length, 2 * byteArray.size))
            readBytes(byteArray, current, byteArray.size - current)
            current = byteArray.size
        }
        return byteArray
    }
}

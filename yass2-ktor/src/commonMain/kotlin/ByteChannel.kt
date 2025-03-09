package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.readBytes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readInt
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeInt

internal suspend fun ByteWriteChannel.writeFully(writer: BytesWriter) {
    writeFully(writer.buffer, 0, writer.current)
}

internal suspend fun ByteReadChannel.read(transport: Transport, length: Int): Any? {
    val buffer =
        @OptIn(InternalApi::class) transport.readBytes(length) { bytes, offset, size -> readFully(bytes, offset, offset + size) }
    return transport.readBytes(buffer)
}

internal suspend fun ByteWriteChannel.write(transport: Transport, value: Any?) {
    @OptIn(InternalApi::class) val writer = transport.createWriter()
    transport.write(writer, value)
    writeInt(writer.current)
    writeFully(writer)
}

internal suspend fun ByteReadChannel.read(transport: Transport) = read(transport, readInt())

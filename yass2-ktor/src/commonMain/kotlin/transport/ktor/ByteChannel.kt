package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.transport.BytesReader
import ch.softappeal.yass2.transport.Transport
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel

internal suspend fun ByteWriteChannel.write(transport: Transport, value: Any?) {
    val writer = transport.createWriter()
    transport.write(writer, value)
    writeInt(writer.current)
    writeFully(writer.buffer, 0, writer.current)
}

internal suspend fun ByteReadChannel.read(transport: Transport, length: Int): Any? {
    val buffer = transport.readBytes(length) { bytes, offset, size -> readFully(bytes, offset, size) }
    val reader = BytesReader(buffer)
    return transport.read(reader).apply {
        check(reader.isDrained)
    }
}

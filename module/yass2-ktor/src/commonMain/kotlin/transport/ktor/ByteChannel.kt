package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.transport.*
import io.ktor.utils.io.*

internal suspend fun ByteWriteChannel.write(transport: Transport, value: Any?) {
    val writer = transport.writer()
    transport.write(writer, value)
    writeInt(writer.current)
    writeFully(writer.buffer, 0, writer.current)
}

internal suspend fun ByteReadChannel.read(transport: Transport, length: Int): Any? {
    val buffer = transport.readBytes(length) { bytes, offset, size -> readFully(bytes, offset, size) }
    val reader = BytesReader(buffer)
    val value = transport.read(reader)
    check(reader.isDrained)
    return value
}

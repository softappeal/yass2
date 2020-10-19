package ch.softappeal.yass2.transport.ktor

import ch.softappeal.yass2.transport.*
import io.ktor.utils.io.*

internal suspend fun ByteWriteChannel.write(config: TransportConfig, value: Any?) {
    val writer = config.writer()
    config.write(writer, value)
    writeInt(writer.current)
    writeFully(writer.buffer, 0, writer.current)
}

internal suspend fun ByteReadChannel.read(config: TransportConfig, length: Int): Any? {
    val buffer = config.readBytes(length) { bytes, offset, size -> readFully(bytes, offset, size) }
    val reader = BytesReader(buffer)
    val value = config.read(reader)
    check(reader.drained)
    return value
}

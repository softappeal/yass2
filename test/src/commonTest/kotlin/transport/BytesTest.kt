package ch.softappeal.yass2.transport

import ch.softappeal.yass2.serialize.*
import kotlin.test.*

fun <T> Serializer.copy(value: T, check: BytesWriter.() -> Unit = {}): T {
    val writer = BytesWriter(1000)
    var size: Int
    with(writer) {
        write(this, value)
        size = current
        check()
    }
    return with(BytesReader(writer.buffer)) {
        @Suppress("UNCHECKED_CAST") val result = read(this) as T
        assertEquals(size, internalCurrent(this))
        result
    }
}

fun BytesWriter.checkTail(vararg bytes: Int) {
    assertEquals(bytes.map { it.toByte() }, buffer.copyOfRange(current - bytes.size, current).toList())
}

class BytesTest {
    @Test
    fun test() {
        var writer = BytesWriter(4)
        with(writer) {
            val buffer = this.buffer
            assertEquals(0, current)
            writeByte(-128)
            assertEquals(1, current)
            assertEquals(-128, buffer[0])
            writeByte(127)
            assertEquals(2, current)
            assertEquals(127, buffer[1])
            writeBytes(ByteArray(0))
            assertEquals(2, current)
            writeBytes(byteArrayOf(1, 2))
            assertEquals(4, current)
            assertEquals(1, buffer[2])
            assertEquals(2, buffer[3])
            checkTail(1, 2)
            assertFails { checkTail(11, 2) }
            assertFails { checkTail(1, 22) }
            writeBytes(ByteArray(0))
            assertEquals(4, current)
            assertSame(buffer, this.buffer)
        }
        with(BytesReader(writer.buffer)) {
            assertFalse(isDrained)
            assertEquals(0, internalCurrent(this))
            assertEquals(-128, readByte())
            assertEquals(1, internalCurrent(this))
            assertEquals(127, readByte())
            assertEquals(2, internalCurrent(this))
            assertFails { readBytes(-1) }
            assertEquals(2, internalCurrent(this))
            assertEquals(0, readBytes(0).size)
            assertEquals(2, internalCurrent(this))
            assertFailsWith<IllegalArgumentException> { readBytes(3) }
            assertEquals(2, internalCurrent(this))
            assertEquals(byteArrayOf(1, 2).toList(), readBytes(2).toList())
            assertEquals(4, internalCurrent(this))
            assertTrue(isDrained)
            assertFailsWith<IllegalArgumentException> { readByte() }
            assertEquals(4, internalCurrent(this))
        }
        writer = BytesWriter(0)
        with(writer) {
            assertEquals(0, writer.buffer.size)
            assertEquals(0, current)
            writeByte(0)
            assertEquals(1000, writer.buffer.size)
            assertEquals(1, current)
        }
        writer = BytesWriter(0)
        with(writer) {
            writeBytes(ByteArray(1000))
            assertEquals(1000, writer.buffer.size)
            assertEquals(1000, current)
            writeByte(0)
            assertEquals(2000, writer.buffer.size)
            assertEquals(1001, current)
        }
        writer = BytesWriter(1000)
        with(writer) {
            writeBytes(ByteArray(1001))
            assertEquals(2000, writer.buffer.size)
            assertEquals(1001, current)
        }
        writer = BytesWriter(1000)
        with(writer) {
            writeBytes(ByteArray(10_000))
            assertEquals(10_000, writer.buffer.size)
            assertEquals(10_000, current)
        }
    }
}

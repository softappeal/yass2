package ch.softappeal.yass2.core.serialize.binary

import ch.softappeal.yass2.core.assertFailsWithMessage
import ch.softappeal.yass2.core.serialize.ByteArrayReader
import ch.softappeal.yass2.core.serialize.ByteArrayWriter
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer
import ch.softappeal.yass2.core.serialize.checkDrained
import kotlin.test.Test
import kotlin.test.assertEquals

fun <T> check(value: T, write: Writer.(value: T) -> Unit, vararg bytes: Int, read: Reader.() -> T) {
    val writer = ByteArrayWriter()
    writer.write(value)
    val byteArray = writer.toyByteArray()
    assertEquals(bytes.map { it.toByte() }, byteArray.toList())
    val reader = ByteArrayReader(byteArray)
    assertEquals(value, reader.read().apply { reader.checkDrained() })
}

class BinaryIntTest {
    @Test
    fun boolean() {
        check(false, { writeBinaryBoolean(it) }, 0) { readBinaryBoolean() }
        check(true, { writeBinaryBoolean(it) }, 1) { readBinaryBoolean() }
        assertFailsWithMessage<IllegalStateException>("unexpected binary boolean 2") {
            ByteArrayReader(byteArrayOf(2)).readBinaryBoolean()
        }
    }
}

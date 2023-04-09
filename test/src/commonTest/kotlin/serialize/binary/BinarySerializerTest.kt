package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.transport.*
import kotlin.test.*

private fun BytesWriter.checkTail(vararg bytes: Int) {
    assertEquals(bytes.map { it.toByte() }, buffer.copyOfRange(current - bytes.size, current).toList())
}

private fun <T> Serializer.copy(value: T, bytes: IntArray): T {
    val writer = BytesWriter(1000)
    with(writer) {
        write(this, value)
        assertEquals(bytes.size, current)
        checkTail(*bytes)
    }
    return with(BytesReader(writer.buffer)) {
        @Suppress("UNCHECKED_CAST") val result = read(this) as T
        assertEquals(bytes.size, internalCurrent(this))
        result
    }
}

val ManyPropertiesConst = ManyProperties(8, 4, 6, 7, 2).apply {
    a = 1
    c = 3
    e = 5
    i = 9
    j = 10
}

private fun ManyProperties.assertManyProperties() {
    assertTrue(
        a == 1 &&
            b == 2 &&
            c == 3 &&
            d == 4 &&
            e == 5 &&
            f == 6 &&
            g == 7 &&
            h == 8 &&
            i == 9 &&
            j == 10
    )
}

fun createGraph(): Node {
    val n1 = Node(1)
    val n2 = Node(2)
    val n3 = Node(3)
    n1.link = n2
    n2.link = n3
    n3.link = n2
    return n1
}

private fun checkGraph(n1: Node) {
    val n2 = n1.link
    val n3 = n2!!.link!!
    assertEquals(1, n1.id)
    assertEquals(2, n2.id)
    assertEquals(3, n3.id)
    assertSame(n3.link, n2)
}

fun duplicatedType(thePackage: String) = assertFailsMessage<IllegalArgumentException>("duplicated type 'class ${thePackage}Int'") {
    BinarySerializer(listOf(IntEncoder, IntEncoder))
}

fun missingType(thePackage: String) = assertFailsMessage<IllegalStateException>("missing type 'class ${thePackage}Boolean'") {
    ContractSerializer.write(BytesWriter(1000), true)
}

open class BinarySerializerTest {
    protected open val serializer = ContractSerializer

    private fun <T> copy(value: T, vararg bytes: Int): T = serializer.copy(value, bytes)

    @Test
    fun testNull() {
        assertNull(copy(null, 0))
    }

    @Test
    fun testInt() {
        assertEquals(60, copy(60, 3, 120))
    }

    @Test
    fun list() {
        assertEquals(0, copy(listOf<String>(), 1, 0).size)
        assertEquals(listOf<Any?>(null, 60), copy(listOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        assertEquals(mutableListOf<Any?>(null, 60), copy(listOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        assertEquals(listOf<Any?>(null, 60), copy(mutableListOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        assertEquals(mutableListOf<Any?>(null, 60), copy(mutableListOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        copy(mutableListOf<Any>(), 1, 0).add(123)
    }

    @Test
    fun intException() {
        with(copy(IntException(null), 6, 0)) { assertNull(i) }
        with(copy(IntException(60), 6, 1, 120)) { assertEquals(60, i) }
    }

    @Test
    fun complexId() {
        with(copy(ComplexId(), 8, 7, 120, 0, 7, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(copy(ComplexId(baseIdOptional = PlainId(61)), 8, 7, 120, 7, 122, 7, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertEquals(61, baseIdOptional!!.id)
            assertTrue(baseIdOptional is PlainId)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(copy(ComplexId(plainId = PlainId(61)), 8, 7, 120, 0, 7, 122, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(61, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(copy(ComplexId(plainIdOptional = PlainId(61)), 8, 7, 120, 0, 7, 118, 7, 122, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(59, plainId.id)
            assertEquals(61, plainIdOptional!!.id)
            assertEquals(58, id)
        }
    }

    @Test
    fun lists() {
        with(copy(Lists(), 9, 0, 0, 0)) {
            assertTrue(list.isEmpty())
            assertNull(listOptional)
            assertTrue(mutableList.isEmpty())
            mutableList.add(PlainId())
            assertEquals(1, mutableList.size)
        }
        with(copy(Lists(list = listOf(PlainId()), mutableList = mutableListOf(PlainId(61))), 9, 1, 7, 120, 0, 1, 7, 122)) {
            assertEquals(1, list.size)
            assertEquals(60, list[0].id)
            assertTrue(list[0] is PlainId)
            assertNull(listOptional)
            assertEquals(1, mutableList.size)
            assertEquals(61, mutableList[0].id)
            assertTrue(mutableList[0] is PlainId)
            mutableList.add(PlainId())
            assertEquals(2, mutableList.size)
        }
        with(copy(Lists(listOptional = listOf()), 9, 0, 1, 0, 0)) {
            assertTrue(list.isEmpty())
            assertTrue(listOptional!!.isEmpty())
            assertTrue(mutableList.isEmpty())
        }
    }

    @Test
    fun idWrapper() {
        with(copy(IdWrapper(), 12, 10, 120, 0)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertNull(idOptional)
        }
        with(copy(IdWrapper(idOptional = Id3()), 12, 10, 120, 11, 120)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertEquals(idOptional!!::class, Id3::class)
            assertEquals(60, idOptional.id)
        }
    }

    @Test
    fun manyProperties() {
        copy(ManyPropertiesConst, 13, 16, 8, 12, 14, 4, 2, 6, 10, 18, 20).assertManyProperties()
    }

    @Test
    fun graph() {
        checkGraph(copy(createGraph(), 15, 2, 15, 4, 15, 6, 2, 1))
    }

    @Test
    fun performance() {
        class WriterReader : Writer, Reader {
            private val buffer = ByteArray(1000)
            var current: Int = 0

            override fun writeByte(byte: Byte) {
                buffer[current++] = byte
            }

            override fun writeBytes(bytes: ByteArray) {
                val newCurrent = current + bytes.size
                bytes.copyInto(buffer, current)
                current = newCurrent
            }

            override fun readByte(): Byte {
                return buffer[current++]
            }

            override fun readBytes(length: Int): ByteArray {
                val newCurrent = current + length
                return ByteArray(length).apply {
                    buffer.copyInto(this, 0, current, newCurrent)
                    current = newCurrent
                }
            }
        }

        val writerReader = WriterReader()
        performance(100_000) {
            writerReader.current = 0
            serializer.write(writerReader, ManyPropertiesConst)
            writerReader.current = 0
            (serializer.read(writerReader) as ManyProperties).assertManyProperties()
        }
    }
}

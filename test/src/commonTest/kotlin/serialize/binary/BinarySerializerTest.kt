package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.ComplexId
import ch.softappeal.yass2.contract.ContractSerializer
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.Id2
import ch.softappeal.yass2.contract.Id3
import ch.softappeal.yass2.contract.IdWrapper
import ch.softappeal.yass2.contract.IntException
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Node
import ch.softappeal.yass2.contract.PlainId
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.performance
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.transport.BytesWriter
import ch.softappeal.yass2.transport.checkTail
import ch.softappeal.yass2.transport.copy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun <T> checkedCopy(value: T, vararg bytes: Int): T = ContractSerializer.copy(value) { checkTail(*bytes) }

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

class BinarySerializerTest {
    @Test
    fun testNull() {
        assertNull(checkedCopy(null, 0))
    }

    @Test
    fun testInt() {
        assertEquals(60, checkedCopy(60, 3, 120))
    }

    @Test
    fun testGender() {
        assertEquals(Gender.Female, checkedCopy(Gender.Female, 6, 0))
        assertEquals(Gender.Male, checkedCopy(Gender.Male, 6, 1))
    }

    @Test
    fun list() {
        assertEquals(0, checkedCopy(listOf<String>(), 1, 0).size)
        assertEquals(listOf<Any?>(null, 60), checkedCopy(listOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        assertEquals(mutableListOf<Any?>(null, 60), checkedCopy(listOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        assertEquals(listOf<Any?>(null, 60), checkedCopy(mutableListOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        assertEquals(mutableListOf<Any?>(null, 60), checkedCopy(mutableListOf<Any?>(null, 60), 1, 2, 0, 3, 120))
        checkedCopy(mutableListOf<Any>(), 1, 0).add(123)
    }

    @Test
    fun intException() {
        with(checkedCopy(IntException(null), 7, 0)) { assertNull(i) }
        with(checkedCopy(IntException(60), 7, 1, 120)) { assertEquals(60, i) }
    }

    @Test
    fun complexId() {
        with(checkedCopy(ComplexId(), 9, 8, 120, 0, 8, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(checkedCopy(ComplexId(baseIdOptional = PlainId(61)), 9, 8, 120, 8, 122, 8, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertEquals(61, baseIdOptional!!.id)
            assertTrue(baseIdOptional is PlainId)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(checkedCopy(ComplexId(plainId = PlainId(61)), 9, 8, 120, 0, 8, 122, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(61, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(checkedCopy(ComplexId(plainIdOptional = PlainId(61)), 9, 8, 120, 0, 8, 118, 8, 122, 116)) {
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
        with(checkedCopy(Lists(), 10, 0, 0, 0)) {
            assertTrue(list.isEmpty())
            assertNull(listOptional)
            assertTrue(mutableList.isEmpty())
            mutableList.add(PlainId())
            assertEquals(1, mutableList.size)
        }
        with(checkedCopy(Lists(list = listOf(PlainId()), mutableList = mutableListOf(PlainId(61))), 10, 1, 8, 120, 0, 1, 8, 122)) {
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
        with(checkedCopy(Lists(listOptional = listOf()), 10, 0, 1, 0, 0)) {
            assertTrue(list.isEmpty())
            assertTrue(listOptional!!.isEmpty())
            assertTrue(mutableList.isEmpty())
        }
    }

    @Test
    fun idWrapper() {
        with(checkedCopy(IdWrapper(), 13, 11, 120, 0)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertNull(idOptional)
        }
        with(checkedCopy(IdWrapper(idOptional = Id3()), 13, 11, 120, 12, 120)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertEquals(idOptional!!::class, Id3::class)
            assertEquals(60, idOptional.id)
        }
    }

    @Test
    fun manyProperties() {
        checkedCopy(ManyPropertiesConst, 14, 16, 8, 12, 14, 4, 2, 6, 10, 18, 20).assertManyProperties()
    }

    @Test
    fun graph() {
        checkGraph(checkedCopy(createGraph(), 17, 2, 17, 4, 17, 6, 2, 1))
    }

    @Test
    fun throwableFake() {
        val throwableFake = ContractSerializer.copy(ThrowableFake("cause", "message"))
        assertEquals("cause", throwableFake.cause)
        assertEquals("message", throwableFake.message)
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
            ContractSerializer.write(writerReader, ManyPropertiesConst)
            writerReader.current = 0
            (ContractSerializer.read(writerReader) as ManyProperties).assertManyProperties()
        }
    }
}

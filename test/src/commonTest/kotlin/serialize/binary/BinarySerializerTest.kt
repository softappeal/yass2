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

fun duplicatedType(thePackage: String) = assertFailsMessage<IllegalArgumentException>("duplicated type 'class ${thePackage}Int'") {
    object : BinarySerializer() {
        init {
            initialize(IntEncoder(), IntEncoder())
        }
    }
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
        assertEquals(60, checkedCopy(60, 2, 120))
    }

    @Test
    fun testGender() {
        assertEquals(Gender.Female, checkedCopy(Gender.Female, 5, 0))
        assertEquals(Gender.Male, checkedCopy(Gender.Male, 5, 1))
    }

    @Test
    fun list() {
        assertEquals(0, checkedCopy(listOf<String>(), 1, 0).size)
        assertEquals(listOf<Any?>(null, 60), checkedCopy(listOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        assertEquals(mutableListOf<Any?>(null, 60), checkedCopy(listOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        assertEquals(listOf<Any?>(null, 60), checkedCopy(mutableListOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        assertEquals(mutableListOf<Any?>(null, 60), checkedCopy(mutableListOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        checkedCopy(mutableListOf<Any>(), 1, 0).add(123)
    }

    @Test
    fun intException() {
        with(checkedCopy(IntException(null), 6, 0)) { assertNull(i) }
        with(checkedCopy(IntException(60), 6, 1, 120)) { assertEquals(60, i) }
    }

    @Test
    fun complexId() {
        with(checkedCopy(ComplexId(), 8, 7, 120, 0, 7, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(checkedCopy(ComplexId(baseIdOptional = PlainId(61)), 8, 7, 120, 7, 122, 7, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertEquals(61, baseIdOptional!!.id)
            assertTrue(baseIdOptional is PlainId)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(checkedCopy(ComplexId(plainId = PlainId(61)), 8, 7, 120, 0, 7, 122, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(61, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(checkedCopy(ComplexId(plainIdOptional = PlainId(61)), 8, 7, 120, 0, 7, 118, 7, 122, 116)) {
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
        with(checkedCopy(Lists(), 9, 0, 0)) {
            assertTrue(list.isEmpty())
            assertNull(listOptional)
        }
        with(checkedCopy(Lists(list = listOf(PlainId())))) {
            assertEquals(1, list.size)
            assertEquals(60, list[0].id)
            assertTrue(list[0] is PlainId)
            assertNull(listOptional)
        }
        with(checkedCopy(Lists(listOptional = listOf()), 9, 0, 1, 0)) {
            assertTrue(list.isEmpty())
            assertTrue(listOptional!!.isEmpty())
        }
    }

    @Test
    fun idWrapper() {
        with(checkedCopy(IdWrapper(), 12, 10, 120, 0)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertNull(idOptional)
        }
        with(checkedCopy(IdWrapper(idOptional = Id3()), 12, 10, 120, 11, 120)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertEquals(idOptional!!::class, Id3::class)
            assertEquals(60, idOptional.id)
        }
    }

    @Test
    fun manyProperties() {
        checkedCopy(ManyPropertiesConst, 13, 16, 8, 12, 14, 4, 2, 6, 10, 18, 20).assertManyProperties()
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

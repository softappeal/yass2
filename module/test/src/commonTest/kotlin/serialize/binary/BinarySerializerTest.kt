package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.transport.*
import kotlin.test.*

private fun <T> Serializer.copy(value: T, bytes: IntArray): T {
    val writer = BytesWriter(1000)
    with(writer) {
        write(this, value)
        assertEquals(bytes.size, current)
        checkTail(*bytes)
    }
    return with(BytesReader(writer.buffer)) {
        @Suppress("UNCHECKED_CAST") val result = read(this) as T
        assertEquals(bytes.size, current)
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

open class BinarySerializerTest {
    protected open val serializer = ContractSerializer

    private fun <T> copy(value: T, vararg bytes: Int): T = serializer.copy(value, bytes)

    @Test
    fun duplicatedType() {
        assertPlatform<IllegalArgumentException>(
            "duplicated type 'class kotlin.Int'",
            "duplicated type 'class Int'",
        ) { BinarySerializer(listOf(IntEncoder, IntEncoder)) }
    }

    @Test
    fun missingType() {
        assertPlatform<IllegalStateException>(
            "missing type 'class kotlin.Boolean'",
            "missing type 'class Boolean'",
        ) { serializer.write(BytesWriter(1000), true) }
    }

    @Test
    fun testNull() {
        assertNull(copy(null, 0))
    }

    @Test
    fun testInt() {
        assertEquals(60, copy(60, 2, 120))
    }

    @Test
    fun list() {
        assertEquals(0, copy(listOf<String>(), 1, 0).size)
        assertEquals(listOf<Any?>(null, 60), copy(listOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        assertEquals(mutableListOf<Any?>(null, 60), copy(listOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        assertEquals(listOf<Any?>(null, 60), copy(mutableListOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        assertEquals(mutableListOf<Any?>(null, 60), copy(mutableListOf<Any?>(null, 60), 1, 2, 0, 2, 120))
        copy(mutableListOf<Any>(), 1, 0).add(123)
    }

    @Test
    fun intException() {
        with(copy(IntException(null), 5, 0)) { assertNull(i) }
        with(copy(IntException(60), 5, 1, 120)) { assertEquals(60, i) }
    }

    @Test
    fun complexId() {
        with(copy(ComplexId(), 7, 6, 120, 0, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(copy(ComplexId(baseIdOptional = PlainId(61)), 7, 6, 120, 6, 122, 118, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertEquals(61, baseIdOptional!!.id)
            assertTrue(baseIdOptional is PlainId)
            assertEquals(59, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(copy(ComplexId(plainId = PlainId(61)), 7, 6, 120, 0, 122, 0, 116)) {
            assertEquals(60, baseId.id)
            assertTrue(baseId is PlainId)
            assertNull(baseIdOptional)
            assertEquals(61, plainId.id)
            assertNull(plainIdOptional)
            assertEquals(58, id)
        }
        with(copy(ComplexId(plainIdOptional = PlainId(61)), 7, 6, 120, 0, 118, 1, 122, 116)) {
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
        with(copy(Lists(), 8, 0, 0, 0)) {
            assertTrue(list.isEmpty())
            assertNull(listOptional)
            assertTrue(mutableList.isEmpty())
            mutableList.add(PlainId())
            assertEquals(1, mutableList.size)
        }
        with(copy(Lists(list = listOf(PlainId()), mutableList = mutableListOf(PlainId(61))), 8, 1, 6, 120, 0, 1, 6, 122)) {
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
        with(copy(Lists(listOptional = listOf()), 8, 0, 1, 0, 0)) {
            assertTrue(list.isEmpty())
            assertTrue(listOptional!!.isEmpty())
            assertTrue(mutableList.isEmpty())
        }
    }

    @Test
    fun idWrapper() {
        with(copy(IdWrapper(), 11, 9, 120, 0)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertNull(idOptional)
        }
        with(copy(IdWrapper(idOptional = Id3()), 11, 9, 120, 10, 120)) {
            assertEquals(id::class, Id2::class)
            assertEquals(60, id.id)
            assertEquals(idOptional!!::class, Id3::class)
            assertEquals(60, idOptional.id)
        }
    }

    @Test
    fun manyProperties() {
        copy(ManyPropertiesConst, 12, 16, 8, 12, 14, 4, 2, 6, 10, 18, 20).assertManyProperties()
    }

    @Test
    fun performance() {
        val buffer = ByteArray(1000)
        performance(100_000) {
            val writer = BytesWriter(buffer)
            serializer.write(writer, ManyPropertiesConst)
            assertSame(buffer, writer.buffer)
            (serializer.read(BytesReader(buffer)) as ManyProperties).assertManyProperties()
        }
    }
}

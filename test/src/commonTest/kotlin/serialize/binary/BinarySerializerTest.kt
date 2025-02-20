package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.contract.A
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.IntException
import ch.softappeal.yass2.contract.IntWrapper
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Optionals
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.contract.TransportSerializer
import ch.softappeal.yass2.contract.Types
import ch.softappeal.yass2.performance
import ch.softappeal.yass2.serialize.BytesReader
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.checkTail
import ch.softappeal.yass2.serialize.readBytes
import ch.softappeal.yass2.serialize.writeBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

fun <T> Serializer.copy(value: T, check: BytesWriter.() -> Unit = {}): T {
    val writer = BytesWriter(1000)
    val size: Int
    with(writer) {
        write(this, value)
        size = current
        check()
    }
    return with(BytesReader(writer.buffer)) {
        @Suppress("UNCHECKED_CAST") val result = read(this) as T
        assertEquals(size, current)
        result
    }
}

private fun <T> checkedCopy(value: T, vararg bytes: Int): T = TransportSerializer.copy(value) {
    assertEquals(current, bytes.size, "actual: ${buffer.copyOf(current).toList()}")
    checkTail(*bytes)
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

fun Serializer.typesTest() {
    with(copy(Types(
        true,
        1,
        2,
        "hello",
        byteArrayOf(1, 2, 3),
        Gender.Female,
    ))) {
        assertTrue(boolean)
        assertEquals(1, int)
        assertEquals(2, long)
        assertEquals("hello", string)
        assertEquals(Gender.Female, gender)
    }
}

class BinarySerializerTest {
    @Test
    fun testNull() {
        assertNull(checkedCopy(null, 0))
    }

    @Test
    fun list() {
        assertEquals(0, checkedCopy(listOf<Int>(), 1, 0).size)
        assertEquals(listOf<Any?>(null, 60), checkedCopy(listOf<Any?>(null, 60), 1, 2, 0, 3, 120))
    }

    @Test
    fun testInt() {
        assertEquals(60, checkedCopy(60, 3, 120))
    }

    @Test
    fun testGender() {
        assertEquals(Gender.Female, checkedCopy(Gender.Female, 7, 0))
        assertEquals(Gender.Male, checkedCopy(Gender.Male, 7, 1))
    }

    @Test
    fun intException() {
        with(checkedCopy(IntException(10), 8, 20)) { assertEquals(10, i) }
    }

    @Test
    fun intWrapper() {
        with(checkedCopy(IntWrapper(10), 9, 20)) { assertEquals(10, i) }
    }

    @Test
    fun typesTest() {
        TransportSerializer.typesTest()
    }

    @Test
    fun optionals() {
        with(checkedCopy(
            Optionals(
                10,
                20,
                IntWrapper(30),
                IntWrapper(40),
            ),
            10,
            20,
            1, 40,
            60,
            1, 80,
        )) {
            assertEquals(10, i)
            assertEquals(20, iOptional)
            assertEquals(30, intWrapper.i)
            assertEquals(40, intWrapperOptional!!.i)
        }
        with(checkedCopy(
            Optionals(
                10,
                null,
                IntWrapper(30),
                null,
            ),
            10,
            20,
            0,
            60,
            0,
        )) {
            assertEquals(10, i)
            assertNull(iOptional)
            assertEquals(30, intWrapper.i)
            assertNull(intWrapperOptional)
        }
    }

    @Test
    fun a() {
        assertEquals(1, A(1).a)
        with(checkedCopy(A(10), 12, 20)) {
            assertEquals(10, a)
        }
    }

    @Test
    fun b() {
        with(B(1, 2)) {
            assertEquals(1, a)
            assertEquals(2, b)
        }
        with(checkedCopy(B(10, 20), 13, 20, 40)) {
            assertEquals(10, a)
            assertEquals(20, b)
        }
    }

    @Test
    fun poly() {
        with(checkedCopy(
            Poly(
                A(10),
                B(30, 40),
            ),
            14,
            12, 20,
            60, 80,
        )) {
            assertEquals(10, a.a)
            assertFalse(a is B)
            assertEquals(30, b.a)
            assertEquals(40, b.b)
        }
        with(checkedCopy(
            Poly(
                B(10, 20),
                B(30, 40),
            ),
            14,
            13, 20, 40,
            60, 80,
        )) {
            assertEquals(10, a.a)
            assertEquals(20, (a as B).b)
            assertEquals(30, b.a)
            assertEquals(40, b.b)
        }
    }

    @Test
    fun lists() {
        with(checkedCopy(
            Lists(
                listOf(10),
                listOf(20),
            ),
            11,
            1, 3, 20,
            1, 1, 3, 40,
        )) {
            assertEquals(listOf(10), list)
            assertEquals(listOf(20), listOptional)
        }
        with(checkedCopy(
            Lists(
                listOf(10),
                null,
            ),
            11,
            1, 3, 20,
            0,
        )) {
            assertEquals(listOf(10), list)
            assertNull(listOptional)
        }
    }

    @Test
    fun manyProperties() {
        checkedCopy(ManyPropertiesConst, 15, 16, 8, 12, 14, 4, 2, 6, 10, 18, 20).assertManyProperties()
    }

    @Test
    fun throwableFake() {
        val throwableFake = TransportSerializer.copy(ThrowableFake("cause", "message"))
        assertEquals("cause", throwableFake.cause)
        assertEquals("message", throwableFake.message)
        with(checkedCopy(
            ThrowableFake(
                null,
                "m",
            ),
            17,
            0,
            1, 109,
        )) {
            assertNull(cause)
            assertEquals("m", message)
        }
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
            TransportSerializer.write(writerReader, ManyPropertiesConst)
            writerReader.current = 0
            (TransportSerializer.read(writerReader) as ManyProperties).assertManyProperties()
        }
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> {
            object : BinarySerializer() {
                init {
                    initialize(IntBinaryEncoder, IntBinaryEncoder)
                }
            }
        }.message!!
        assertTrue(message.startsWith("duplicated type 'class "))
        assertTrue(message.endsWith("Int'"))
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> {
            TransportSerializer.write(BytesWriter(1000), 1.2)
        }.message!!
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.endsWith("Double'"))
    }

    @Test
    fun bytes() {
        assertEquals(
            "hello",
            with(TransportSerializer) { readBytes(writeBytes("hello")) }
        )
    }
}

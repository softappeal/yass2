package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.contract.A
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.contract.createBinarySerializer
import ch.softappeal.yass2.serialize.BytesReader
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.checkTail
import ch.softappeal.yass2.serialize.readBytes
import ch.softappeal.yass2.serialize.string.AllBaseTypes
import ch.softappeal.yass2.serialize.string.allBaseTypesAssert
import ch.softappeal.yass2.serialize.writeBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val SERIALIZER = createBinarySerializer()

private fun <T> Serializer.copy(value: T, check: BytesWriter.() -> Unit = {}): T {
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

private fun <T> checkedCopy(value: T, vararg bytes: Int): T = SERIALIZER.copy(value) {
    assertEquals(current, bytes.size, "actual: ${toyByteArray().toList()}")
    checkTail(*bytes)
}

class BinarySerializerTest {
    @Test
    fun allBaseTypes() {
        fun Serializer.allBaseTypesTest(serialized: ByteArray) {
            assertContentEquals(serialized, writeBytes(AllBaseTypes))
            allBaseTypesAssert(serialized)
        }
        SERIALIZER.allBaseTypesTest(byteArrayOf(
            15,
            1,
            2,
            4,
            64,
            94,
            -35,
            47,
            26,
            -97,
            -66,
            119,
            5,
            104,
            101,
            108,
            108,
            111,
            3,
            0,
            1,
            2,
            0,
            15,
            0,
            2,
            0,
            2,
            1,
            3,
            1,
            4,
            4,
            5,
            64,
            94,
            -35,
            47,
            26,
            -97,
            -66,
            119,
            6,
            5,
            104,
            101,
            108,
            108,
            111,
            7,
            3,
            0,
            1,
            2,
            8,
            1,
            1,
            2,
            3,
            2,
            1,
            2,
            6,
            5,
            104,
            101,
            108,
            108,
            111,
            6,
            5,
            119,
            111,
            114,
            108,
            100,
            15,
            1,
            -1,
            -120,
            15,
            -2,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            1,
            64,
            94,
            -35,
            47,
            26,
            -97,
            -66,
            119,
            5,
            104,
            101,
            108,
            108,
            111,
            3,
            0,
            1,
            2,
            0,
            0,
            2,
            4,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            13,
            20,
            20,
            20,
            0,
            11,
            10,
            20,
            40,
            2,
            4,
            12,
            16,
            8,
            12,
            14,
            4,
            2,
            6,
            10,
            18,
            20,
            20,
            40,
            1,
            1,
            1,
            2,
            1,
            4,
            1,
            64,
            94,
            -35,
            47,
            26,
            -97,
            -66,
            119,
            1,
            5,
            104,
            101,
            108,
            108,
            111,
            1,
            3,
            0,
            1,
            2,
            1,
            0,
            1,
            1,
            6,
            5,
            104,
            101,
            108,
            108,
            111,
            1,
            60,
            80,
        ))
    }

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
    fun int() {
        assertEquals(60, checkedCopy(60, 3, 120))
    }

    @Test
    fun gender() {
        assertEquals(Gender.Female, checkedCopy(Gender.Female, 8, 0))
        assertEquals(Gender.Male, checkedCopy(Gender.Male, 8, 1))
    }

    @Test
    fun a() {
        assertEquals(1, A(1).a)
        with(checkedCopy(A(10), 9, 20)) {
            assertEquals(10, a)
        }
    }

    @Test
    fun b() {
        with(B(1, 2)) {
            assertEquals(1, a)
            assertEquals(2, b)
        }
        with(checkedCopy(B(10, 20), 10, 20, 40)) {
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
            11,
            9, 20,
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
            11,
            10, 20, 40,
            60, 80,
        )) {
            assertEquals(10, a.a)
            assertEquals(20, (a as B).b)
            assertEquals(30, b.a)
            assertEquals(40, b.b)
        }
    }

    @Test
    fun throwableFake() {
        val throwableFake = SERIALIZER.copy(ThrowableFake("cause", "message"))
        assertEquals("cause", throwableFake.cause)
        assertEquals("message", throwableFake.message)
        with(checkedCopy(
            ThrowableFake(
                null,
                "m",
            ),
            14,
            0,
            1, 109,
        )) {
            assertNull(cause)
            assertEquals("m", message)
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
            SERIALIZER.write(BytesWriter(1000), BinarySerializerTest())
        }.message!!
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.endsWith("BinarySerializerTest'"))
    }

    @Test
    fun bytes() {
        assertEquals(
            "hello",
            with(SERIALIZER) { readBytes(writeBytes("hello")) }
        )
    }
}

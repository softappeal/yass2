package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.B
import ch.softappeal.yass2.serialize.BinarySerializer
import ch.softappeal.yass2.serialize.ByteArrayReader
import ch.softappeal.yass2.serialize.ByteArrayWriter
import ch.softappeal.yass2.serialize.Example
import ch.softappeal.yass2.serialize.Poly
import ch.softappeal.yass2.serialize.checkDrained
import ch.softappeal.yass2.serialize.fromByteArray
import ch.softappeal.yass2.serialize.read
import ch.softappeal.yass2.serialize.toByteArray
import ch.softappeal.yass2.serialize.write
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val SERIALIZER = BinarySerializer()

private fun check(value: Any?, vararg bytes: Int) {
    val byteArray = SERIALIZER.toByteArray(value)
    assertEquals(bytes.map { it.toByte() }, byteArray.toList())
    assertEquals(value, SERIALIZER.fromByteArray(byteArray))
}

class BinarySerializerTest {
    @Test
    fun builtIn() {
        check(null, 0)

        check(listOf<Int>(), 1, 0)
        check(listOf(null), 1, 1, 0)
    }

    @Test
    fun example() {
        check(
            Example(
                int = 10,
                intOptional = null,
                any = 11,
                anyOptional = null,
                list = listOf(12, 13),
                listOptional = null,
            ),
            7,               // Example
            20,              // int
            0,               // intOptional
            2, 22,           // any
            0,               // anyOptional
            2, 2, 24, 2, 26, // list
            0,               // listOptional
        )

        check(
            Example(
                int = 10,
                intOptional = 14,
                any = 11,
                anyOptional = 15,
                list = listOf(12, 13),
                listOptional = listOf(16, 17),
            ),
            7,                 // Example
            20,                // int
            1, 28,             // intOptional
            2, 22,             // any
            2, 30,             // anyOptional
            2, 2, 24, 2, 26,   // list
            1, 2, 2, 32, 2, 34 // listOptional
        )
    }

    @Test
    fun poly() {
        check(
            Poly(
                a = B(
                    a = 10,
                    b = 20,
                ),
                b = B(
                    a = 1,
                    b = 2,
                ),
            ),
            6,         // Poly
            5, 20, 40, // a
            2, 4,      // b
        )
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
        println(message)
        assertTrue(message.startsWith("duplicated type '"))
        assertTrue(message.contains(Int::class.simpleName!!))
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> { SERIALIZER.toByteArray(BinarySerializerTest()) }.message!!
        println(message)
        assertTrue(message.startsWith("missing type '"))
        assertTrue(message.contains(BinarySerializerTest::class.simpleName!!))
    }

    @Test
    fun testExtensionFunctions() {
        val writer = ByteArrayWriter()
        SERIALIZER.write(writer, 123)
        val reader = ByteArrayReader(writer.toByteArray())
        assertEquals(123, SERIALIZER.read(reader))
        reader.checkDrained()
    }
}

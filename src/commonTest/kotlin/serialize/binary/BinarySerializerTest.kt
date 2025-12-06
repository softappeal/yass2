package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.Example
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.serialize.fromByteArray
import ch.softappeal.yass2.serialize.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val SERIALIZER = ch.softappeal.yass2.BinarySerializer

private fun check(value: Any?, vararg bytes: Int) {
    val byteArray = SERIALIZER.toByteArray(value)
    assertEquals(bytes.map { it.toByte() }, byteArray.toList())
    assertEquals(value, SERIALIZER.fromByteArray(byteArray))
}

class BinarySerializerTest {
    @Test
    fun builtIn() {
        check(null, 0)

        check(60, 3, 120)

        check(listOf<Int>(), 1, 0)
        check(listOf(null, 60), 1, 2, 0, 3, 120)
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
            18,              // Example
            20,              // int
            0,               // intOptional
            3, 22,           // any
            0,               // anyOptional
            2, 3, 24, 3, 26, // list
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
            18,                // Example
            20,                // int
            1, 28,             // intOptional
            3, 22,             // any
            3, 30,             // anyOptional
            2, 3, 24, 3, 26,   // list
            1, 2, 3, 32, 3, 34 // listOptional
        )
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> {
            object : BinarySerializer() {
                init {
                    initialize(GenderEncoder, GenderEncoder)
                }
            }
        }.message!!
        println(message)
        assertTrue(message.startsWith("duplicated type '"))
        assertTrue(message.contains(Gender::class.simpleName!!))
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> { SERIALIZER.toByteArray(BinarySerializerTest()) }.message!!
        println(message)
        assertTrue(message.startsWith("missing type '"))
        assertTrue(message.contains(BinarySerializerTest::class.simpleName!!))
    }
}

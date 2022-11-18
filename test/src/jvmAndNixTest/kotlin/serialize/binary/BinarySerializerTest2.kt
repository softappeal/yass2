package ch.softappeal.yass2.serialize.binary

import kotlin.test.*

class BinarySerializerTest2 {
    @Test
    fun duplicatedType() = duplicatedType("kotlin.")

    @Test
    fun missingType() = missingType("kotlin.")
}

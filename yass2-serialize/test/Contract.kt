package ch.softappeal.yass2.serialize

import ch.softappeal.yass2.serialize.binary.BinaryEncoders
import ch.softappeal.yass2.serialize.binary.BooleanBinaryEncoder
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.serialize.text.IntTextEncoder
import ch.softappeal.yass2.serialize.text.TextEncoders

enum class Gender { Female, Male }

open class A(open val a: Int?)

data class B(override val a: Int, val b: Int) : A(a)

data class Poly(
    val a: A,
    val b: B,
)

data class Example(
    val int: Int,
    val intOptional: Int?,
    val any: Any,
    val anyOptional: Any?,
    val list: List<Int>,
    val listOptional: List<Int>?,
)

data class TextBuiltIn(
    val boolean: Boolean,
    val booleanOptional: Boolean?,
    val string: String,
    val stringOptional: String?,
)

@BinaryEncoders(
    IntBinaryEncoder::class,
    BooleanBinaryEncoder::class,
    StringBinaryEncoder::class,
)
@TextEncoders(
    IntTextEncoder::class,
)
@ConcreteAndEnumClasses(
    A::class,
    B::class,
    Poly::class,
    Example::class,
    TextBuiltIn::class,
)
internal class Generate

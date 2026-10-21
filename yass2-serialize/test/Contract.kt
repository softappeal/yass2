package ch.softappeal.yass2.serialize

import ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.serialize.binary.BooleanBinaryEncoder
import ch.softappeal.yass2.serialize.binary.ByteArrayBinaryEncoder
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.serialize.binary.LongBinaryEncoder
import ch.softappeal.yass2.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.serialize.string.ByteArrayStringEncoder
import ch.softappeal.yass2.serialize.string.IntStringEncoder
import ch.softappeal.yass2.serialize.string.LongStringEncoder
import ch.softappeal.yass2.serialize.string.StringEncoderObjects

enum class Gender { Female, Male }

class Types(
    val boolean: Boolean,
    val int: Int,
    val long: Long,
    val string: String,
    val bytes: ByteArray,
    val gender: Gender,
    val list: List<Any?>,
    val b: B,
    val booleanOptional: Boolean?,
    val intOptional: Int?,
    val longOptional: Long?,
    val stringOptional: String?,
    val bytesOptional: ByteArray?,
    val genderOptional: Gender?,
    val listOptional: List<Any?>?,
    val bOptional: B?,
)

open class A(open val a: Int)

class B(override val a: Int, val b: Int) : A(123)

class Poly(
    val a: A,
    val b: B,
)

class ManyProperties(
    var h: Int,
    val d: Int,
    val f: Int,
    val g: Int,
    var b: Int,
)

class DivideByZeroException

class ThrowableFake(
    val cause: String?,
    val message: String,
)

data class Example(
    val int: Int,
    val intOptional: Int?,
    val any: Any,
    val anyOptional: Any?,
    val list: List<Int>,
    val listOptional: List<Int>?,
)

@BinaryEncoderObjects(
    BooleanBinaryEncoder::class,
    IntBinaryEncoder::class,
    LongBinaryEncoder::class,
    StringBinaryEncoder::class,
    ByteArrayBinaryEncoder::class,
)
@StringEncoderObjects(
    IntStringEncoder::class,
    LongStringEncoder::class,
    ByteArrayStringEncoder::class,
)
@ConcreteAndEnumClasses(
    Gender::class,
    A::class,
    B::class,
    Poly::class,
    ManyProperties::class,
    DivideByZeroException::class,
    ThrowableFake::class,
    Types::class,
    Example::class,
)
internal class Generate

package ch.softappeal.yass2.contract

import ch.softappeal.yass2.core.NotJsPlatform
import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.ValueReply
import ch.softappeal.yass2.core.serialize.binary.BooleanBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.ByteArrayBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.DoubleBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.LongBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.core.serialize.string.ByteArrayStringEncoder
import ch.softappeal.yass2.core.serialize.string.DoubleStringEncoder
import ch.softappeal.yass2.core.serialize.string.IntStringEncoder
import ch.softappeal.yass2.core.serialize.string.LongStringEncoder
import ch.softappeal.yass2.core.serialize.string.TextSerializer
import ch.softappeal.yass2.coroutines.session.Packet

enum class Gender { Female, Male }

class Types(
    val boolean: Boolean,
    val int: Int,
    val long: Long,
    val double: Double,
    val string: String,
    val bytes: ByteArray,
    val gender: Gender,
    val list: List<Any?>,
    val b: B,
    val booleanOptional: Boolean?,
    val intOptional: Int?,
    val longOptional: Long?,
    val doubleOptional: Double?,
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
    val b: Int,
) {
    var e: Int = 0
    var j: Int = 0
    var c: Int = 0
    var a: Int = 0
    var i: Int = 0
}

class DivideByZeroException : RuntimeException()

class ThrowableFake(
    val cause: String?,
    val message: String,
)

class BodyProperty {
    var body: Any? = null
}

interface AddCalculator {
    suspend fun add(a: Int, b: Int): Int
}

interface Calculator : AddCalculator {
    suspend fun divide(a: Int, b: Int): Int
}

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class TestAnnotation

interface Echo {
    @TestAnnotation
    suspend fun echo(value: Any?): Any?
    suspend fun echoRequired(value: Any): Any
    suspend fun noParametersNoResult()
    suspend fun delay(milliSeconds: Int)
    suspend fun echoMonster(a: List<*>, b: List<List<String?>?>, c: Map<out Int, String>, d: Pair<*, *>): Map<in Int, String>?
    suspend fun echoException(value: Exception): Exception
}

internal val ConcreteClasses = listOf(
    Gender::class,
    A::class,
    B::class,
    Poly::class,
    ManyProperties::class,
    DivideByZeroException::class,
    ThrowableFake::class,
    Types::class,
    Request::class, ValueReply::class, ExceptionReply::class,
    Packet::class,
    BodyProperty::class,
)

internal val BinaryEncoderObjects = listOf(
    BooleanBinaryEncoder::class,
    IntBinaryEncoder::class,
    LongBinaryEncoder::class,
    DoubleBinaryEncoder::class,
    StringBinaryEncoder::class,
    ByteArrayBinaryEncoder::class,
)

internal val StringEncoderObjects = listOf(
    IntStringEncoder::class,
    LongStringEncoder::class,
    @OptIn(NotJsPlatform::class) DoubleStringEncoder::class,
    ByteArrayStringEncoder::class,
)

val TransportSerializer = TextSerializer(StringEncoders)

val CalculatorId: ServiceId<Calculator> = ServiceId("calc")
val EchoId: ServiceId<Echo> = ServiceId("echo")

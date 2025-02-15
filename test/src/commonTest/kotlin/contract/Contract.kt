package ch.softappeal.yass2.contract

import ch.softappeal.yass2.coroutines.Packet
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.serialize.Transport
import ch.softappeal.yass2.serialize.binary.BooleanBinaryEncoder
import ch.softappeal.yass2.serialize.binary.ByteArrayBinaryEncoder
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.serialize.binary.LongBinaryEncoder
import ch.softappeal.yass2.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.serialize.string.BooleanStringEncoder
import ch.softappeal.yass2.serialize.string.ByteArrayStringEncoder
import ch.softappeal.yass2.serialize.string.IntStringEncoder
import ch.softappeal.yass2.serialize.string.LongStringEncoder

enum class Gender { Female, Male }

class Types(
    val boolean: Boolean,
    val int: Int,
    val long: Long,
    val string: String,
    val bytes: ByteArray,
    val gender: Gender,
)

class IntException(val i: Int) : RuntimeException()

class IntWrapper(val i: Int)

class Optionals(
    val i: Int,
    val iOptional: Int?,
    val intWrapper: IntWrapper,
    val intWrapperOptional: IntWrapper?,
)

class Lists(
    val list: List<Int>,
    val listOptional: List<Int>?,
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

interface Mixed {
    fun divide(a: Int, b: Int): Int
    suspend fun suspendDivide(a: Int, b: Int): Int
    fun noParametersNoResult()
}

internal val ConcreteClasses = listOf(
    Gender::class,
    IntException::class,
    IntWrapper::class,
    Optionals::class,
    Lists::class,
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
    StringBinaryEncoder::class,
    ByteArrayBinaryEncoder::class,
)

internal val StringEncoderObjects = listOf(
    BooleanStringEncoder::class,
    IntStringEncoder::class,
    LongStringEncoder::class,
    ByteArrayStringEncoder::class,
)

val TransportSerializer = createBinarySerializer()

val ContractTransport = Transport(TransportSerializer)

val CalculatorId: ServiceId<Calculator> = ServiceId("calc")
val EchoId: ServiceId<Echo> = ServiceId("echo")

const val DEMO_HEADER_KEY = "Demo-Header-Key"
const val DEMO_HEADER_VALUE = "Demo-Header-Value"

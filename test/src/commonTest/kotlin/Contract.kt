package ch.softappeal.yass2

import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.ValueReply
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.binary.BooleanBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.ByteArrayBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.core.serialize.string.ByteArrayStringEncoder
import ch.softappeal.yass2.core.serialize.string.IntStringEncoder
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.core.serialize.string.TextSerializer
import ch.softappeal.yass2.coroutines.session.Packet

enum class Gender { Female, Male }

class Types(
    val boolean: Boolean,
    val int: Int,
    val string: String,
    val bytes: ByteArray,
    val gender: Gender,
    val list: List<Any?>,
    val b: B,
    val booleanOptional: Boolean?,
    val intOptional: Int?,
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

class DivideByZeroException : RuntimeException()

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

interface GenericService<A, B, C> {
    suspend fun service(a: A, b: B): C
}

val ContractSerializer = TextSerializer(stringEncoders())

val CalculatorId = ServiceId<Calculator>("calc")
val EchoId = ServiceId<Echo>("echo")

@Proxies(
    Calculator::class,
    Echo::class,
    GenericService::class,
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
    Request::class, ValueReply::class, ExceptionReply::class,
    Packet::class,
    Example::class,
)
@BinaryEncoderObjects(
    BooleanBinaryEncoder::class,
    IntBinaryEncoder::class,
    StringBinaryEncoder::class,
    ByteArrayBinaryEncoder::class,
)
@StringEncoderObjects(
    IntStringEncoder::class,
    ByteArrayStringEncoder::class,
)
object Generate

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.remote.ContractException
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Proxies
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.remote.session.Packet
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.serialize.binary.BinaryEncoders
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.serialize.text.IntTextEncoder
import ch.softappeal.yass2.serialize.text.TextEncoders

enum class Gender { Female, Male }

open class A(open val a: Int)

class B(override val a: Int, val b: Int) : A(123)

class Poly(
    val a: A,
    val b: B,
)

class DivideByZeroException : ContractException()

class ThrowableFake(
    val cause: String?,
    val message: String,
)

data class Example(
    val boolean: Boolean,
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
    suspend fun noParametersNoResult()
    suspend fun delay(milliSeconds: Int)
}

interface GenericService<in A : Number, B, out C> {
    suspend fun service(a: A, b: B): C
    suspend fun echoMonster(a: List<*>, b: List<List<String?>?>, c: Map<out Int, String>, d: Pair<*, *>): Map<in Int, String>?
}

@Proxies(
    Calculator::class,
    GenericService::class,
)
@BinaryEncoders(
    IntBinaryEncoder::class,
    StringBinaryEncoder::class,
)
@TextEncoders(
    IntTextEncoder::class,
)
@ConcreteAndEnumClasses(
    Gender::class,
    A::class,
    B::class,
    Poly::class,
    DivideByZeroException::class,
    ThrowableFake::class,
    Request::class, ValueReply::class, ExceptionReply::class,
    Packet::class,
    Example::class,
)
internal class Generate

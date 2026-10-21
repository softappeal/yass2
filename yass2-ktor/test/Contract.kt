package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.remote.ContractException
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Proxies
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.remote.session.Packet
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.serialize.binary.BinaryEncoders
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.serialize.string.IntStringEncoder
import ch.softappeal.yass2.serialize.string.StringEncoders
import ch.softappeal.yass2.serialize.string.TextSerializer

class DivideByZeroException : ContractException()

interface Calculator {
    suspend fun add(a: Int, b: Int): Int
    suspend fun divide(a: Int, b: Int): Int
    suspend fun noParametersNoResult()
    suspend fun delay(milliSeconds: Int)
}

val ContractSerializer = TextSerializer(stringEncoders())

val CalculatorId = ServiceId<Calculator>("Calculator")

@Proxies(
    Calculator::class,
)
@BinaryEncoders(
    IntBinaryEncoder::class,
    StringBinaryEncoder::class,
)
@StringEncoders(
    IntStringEncoder::class,
)
@ConcreteAndEnumClasses(
    DivideByZeroException::class,
    Request::class, ValueReply::class, ExceptionReply::class,
    Packet::class,
)
internal class Generate

package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.remote.ContractException
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Proxies
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.remote.session.Packet
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
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
    DivideByZeroException::class,
    Request::class, ValueReply::class, ExceptionReply::class,
    Packet::class,
)
internal class Generate

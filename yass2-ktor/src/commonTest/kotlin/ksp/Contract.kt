package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.A
import ch.softappeal.yass2.B
import ch.softappeal.yass2.Calculator
import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.Echo
import ch.softappeal.yass2.Example
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.GenericService
import ch.softappeal.yass2.ManyProperties
import ch.softappeal.yass2.Poly
import ch.softappeal.yass2.ThrowableFake
import ch.softappeal.yass2.Types
import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Request
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
import ch.softappeal.yass2.coroutines.session.Packet

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

const val CONTRACT_KSP_PGM = """
package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.A
import ch.softappeal.yass2.B
import ch.softappeal.yass2.Calculator
import ch.softappeal.yass2.DivideByZeroException
import ch.softappeal.yass2.Echo
import ch.softappeal.yass2.Example
import ch.softappeal.yass2.Gender
import ch.softappeal.yass2.GenericService
import ch.softappeal.yass2.ManyProperties
import ch.softappeal.yass2.Poly
import ch.softappeal.yass2.ThrowableFake
import ch.softappeal.yass2.Types
import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Request
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
import ch.softappeal.yass2.coroutines.session.Packet

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
"""

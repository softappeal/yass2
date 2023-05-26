@file:GenerateBinarySerializer(
    baseEncoderClasses = [
        IntEncoder::class,
        StringEncoder::class,
        ByteArrayEncoder::class,
        GenderEncoder::class,
    ],
    treeConcreteClasses = [
        IntException::class, PlainId::class, ComplexId::class, Lists::class,
        Id2::class, Id3::class, IdWrapper::class, ManyProperties::class,
        DivideByZeroException::class,
        ThrowableFake::class,
    ],
    graphConcreteClasses = [
        Node::class
    ],
)

package ch.softappeal.yass2.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*

val BaseEncoderClasses = listOf(
    IntEncoder::class,
    StringEncoder::class,
    ByteArrayEncoder::class,
    GenderEncoder::class,
)

val TreeConcreteClasses = listOf(
    IntException::class, PlainId::class, ComplexId::class, Lists::class,
    Id2::class, Id3::class, IdWrapper::class, ManyProperties::class,
    DivideByZeroException::class,
    ThrowableFake::class,
)

val GraphConcreteClasses = listOf(
    Node::class
)

internal class GenderEncoder : EnumEncoder<Gender>(Gender::class, enumValues())

val ContractSerializer = GeneratedBinarySerializer
val MessageSerializer = binaryMessageSerializer(ContractSerializer)
val PacketSerializer = binaryPacketSerializer(MessageSerializer)

val CalculatorId: ServiceId<Calculator> = ServiceId(1)
val EchoId: ServiceId<Echo> = ServiceId(2)
val FlowServiceId: ServiceId<FlowService> = ServiceId(3)

fun StringBuilder.valueDumper(value: Any) {
    when (value) {
        is ByteArray -> append("binary")
    }
}

val MessageTransport = Transport(MessageSerializer, 100, 100)
val PacketTransport = Transport(PacketSerializer, 100, 100)

const val DEMO_HEADER_KEY = "Demo-Header-Key"
const val DEMO_HEADER_VALUE = "Demo-Header-Value"

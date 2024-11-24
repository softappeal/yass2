package ch.softappeal.yass2.contract

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.ValueDumper
import ch.softappeal.yass2.contract.reflect.createDumper
import ch.softappeal.yass2.contract.reflect.createSerializer
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.ByteArrayEncoder
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.serialize.binary.StringEncoder
import ch.softappeal.yass2.transport.Transport
import ch.softappeal.yass2.transport.binaryMessageSerializer
import ch.softappeal.yass2.transport.session.binaryPacketSerializer

expect fun createSerializer(): BinarySerializer

@GenerateBinarySerializer(
    baseEncoderClasses = [
        IntEncoder::class,
        StringEncoder::class,
        ByteArrayEncoder::class,
    ],
    enumClasses = [
        Gender::class,
    ],
    treeConcreteClasses = [
        IntException::class, PlainId::class, ComplexId::class, Lists::class,
        Id2::class, Id3::class, IdWrapper::class, ManyProperties::class,
        DivideByZeroException::class,
        ThrowableFake::class,
    ],
    graphConcreteClasses = [
        Node::class,
    ],
    withDumper = true,
)
val ContractSerializer = createSerializer()
val MessageSerializer = binaryMessageSerializer(ContractSerializer)
val PacketSerializer = binaryPacketSerializer(MessageSerializer)

val CalculatorId: ServiceId<Calculator> = ServiceId(1)
val EchoId: ServiceId<Echo> = ServiceId(2)

expect fun createDumper(dumpValue: ValueDumper): Dumper

val Dumper = createDumper { value ->
    when (value) {
        is ByteArray -> append("binary")
    }
}

val MessageTransport = Transport(MessageSerializer, 100, 100)
val PacketTransport = Transport(PacketSerializer, 100, 100)

const val DEMO_HEADER_KEY = "Demo-Header-Key"
const val DEMO_HEADER_VALUE = "Demo-Header-Value"

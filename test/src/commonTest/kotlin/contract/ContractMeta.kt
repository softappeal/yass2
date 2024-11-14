package ch.softappeal.yass2.contract

import ch.softappeal.yass2.contract.reflect.createDumper
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.binary.ByteArrayEncoder
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.serialize.binary.StringEncoder
import ch.softappeal.yass2.transport.Transport
import ch.softappeal.yass2.transport.binaryMessageSerializer
import ch.softappeal.yass2.transport.session.binaryPacketSerializer

// TODO: shows workaround for https://slack-chats.kotlinlang.org/t/16366233/i-m-trying-out-kotlin-2-0-beta-3-and-it-looks-like-generated
//   It's expected that common code cannot reference generated code in the compilation of platform code.
//   Generated codes are treated as platform code and K2 explicitly disallow references from common to platform (you'll have to use expect/actual).
expect fun createContractSerializer(): Serializer

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
val ContractSerializer = createContractSerializer()

val MessageSerializer = binaryMessageSerializer(ContractSerializer)
val PacketSerializer = binaryPacketSerializer(MessageSerializer)

val CalculatorId: ServiceId<Calculator> = ServiceId(1)
val EchoId: ServiceId<Echo> = ServiceId(2)

val Dumper = createDumper { value ->
    when (value) {
        is ByteArray -> append("binary")
    }
}

val MessageTransport = Transport(MessageSerializer, 100, 100)
val PacketTransport = Transport(PacketSerializer, 100, 100)

const val DEMO_HEADER_KEY = "Demo-Header-Key"
const val DEMO_HEADER_VALUE = "Demo-Header-Value"

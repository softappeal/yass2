package ch.softappeal.yass2.contract

import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.serialize.binary.ByteArrayEncoder
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.serialize.binary.StringEncoder
import ch.softappeal.yass2.serialize.binary.enumEncoder
import ch.softappeal.yass2.transport.Transport
import ch.softappeal.yass2.transport.binaryMessageSerializer
import ch.softappeal.yass2.transport.session.binaryPacketSerializer

internal val BaseEncoders = listOf(
    IntEncoder,
    StringEncoder,
    ByteArrayEncoder,
    enumEncoder<Gender>(),
)

val ContractSerializer = createSerializer(BaseEncoders)
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

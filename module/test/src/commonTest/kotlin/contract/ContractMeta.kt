package ch.softappeal.yass2.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*

val BaseEncoders = listOf(IntEncoder, StringEncoder, ByteArrayEncoder)

val TreeConcreteClasses = listOf(
    IntException::class, PlainId::class, ComplexId::class, Lists::class,
    Id2::class, Id3::class, IdWrapper::class, ManyProperties::class,
    DivideByZeroException::class,
)

val GraphConcreteClasses = listOf(
    Node::class
)

val GeneratedSerializer = generatedBinarySerializer(BaseEncoders)
val MessageSerializer = binaryMessageSerializer(GeneratedSerializer)
val PacketSerializer = binaryPacketSerializer(MessageSerializer)

val CalculatorId: ServiceId<Calculator> = serviceId(1)
val EchoId: ServiceId<Echo> = serviceId(2)
val FlowServiceId: ServiceId<FlowService> = serviceId(3)

val ServiceIds = listOf(CalculatorId, EchoId, FlowServiceId)

val ValueDumper: ValueDumper = { value ->
    when (value) {
        is ByteArray -> append("binary")
        is Color -> append(value.name)
    }
}

val MessageConfig = TransportConfig(MessageSerializer, 100, 100)
val PacketConfig = TransportConfig(PacketSerializer, 100, 100)

const val DemoHeaderKey = "Demo-Header-Key"
const val DemoHeaderValue = "Demo-Header-Value"

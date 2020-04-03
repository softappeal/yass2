package ch.softappeal.yass2.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.binary.*

val BaseEncoders = listOf(IntEncoder, StringEncoder, ByteArrayEncoder)

val ConcreteClasses = listOf(
    IntException::class, PlainId::class, ComplexId::class, Lists::class,
    Id2::class, Id3::class, IdWrapper::class, ManyProperties::class,
    Request::class, ValueReply::class, ExceptionReply::class, DivideByZeroException::class, Packet::class,
    Node::class
)

val GeneratedSerializer = generatedBinarySerializer(BaseEncoders)

val CalculatorId = serviceId<Calculator>(1)
val EchoId = serviceId<Echo>(2)
val FlowServiceId = serviceId<FlowService>(3)

val ServiceIds = listOf(CalculatorId, EchoId, FlowServiceId)

val BaseDumper: BaseDumper = { value ->
    when (value) {
        is ByteArray -> append("binary")
        is Color -> append(value.name)
    }
}

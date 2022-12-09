package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*
import ch.softappeal.yass2.tutorial.contract.generated.*
import kotlin.reflect.*

// This file describes the needed contract metadata.

@Suppress("PrivatePropertyName")
// Shows how to implement an own base type encoder.
private val MyDateEncoder = BaseEncoder(MyDate::class,
    { writer, value -> writer.writeLong(value.currentTimeMillis) },
    { reader -> MyDate(reader.readLong()) }
)

/** Define all the base encoders needed by the contract (including enumerations and own base types). */
public val BaseEncoders: List<BaseEncoder<out Any>> = listOf(
    IntEncoder,
    StringEncoder,
    enumEncoder<Gender>(),
    MyDateEncoder,
)

/** Define all the concrete classes needed by the contract. */
public val ConcreteClasses: List<KClass<out Any>> = listOf(
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
)

/** Define the [ServiceId] for each contract interface. */
public val CalculatorId: ServiceId<Calculator> = serviceId(1)
public val NewsListenerId: ServiceId<NewsListener> = serviceId(2)

/** Define all used [ServiceId]. */
public val ServiceIds: List<ServiceId<out Any>> = listOf(CalculatorId, NewsListenerId)

/** Writes value (without line breaks) if responsible else does nothing. */
public fun StringBuilder.valueDumper(value: Any) {
    when (value) {
        is MyDate -> append("MyDate(${value.currentTimeMillis})")
    }
}

public val ContractSerializer: BinarySerializer = generatedBinarySerializer(BaseEncoders)
public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

public val MessageTransport: Transport = Transport(MessageSerializer, 100)
public val PacketTransport: Transport = Transport(PacketSerializer, 100)

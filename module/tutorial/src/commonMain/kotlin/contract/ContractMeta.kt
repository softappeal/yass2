package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*
import ch.softappeal.yass2.tutorial.contract.generated.*
import kotlin.reflect.*

// This file describes the needed contract metadata.

// Shows how to implement an own base type encoder.
private val MyDateEncoder = BaseEncoder(MyDate::class,
    { writer, value -> writer.writeLong(value.currentTimeMillis) },
    { reader -> MyDate(reader.readLong()) }
)

/** Define all the base encoders needed by the contract (including enumerations and own base types). */
public fun baseEncoders(): List<BaseEncoder<out Any>> = listOf(
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
private fun StringBuilder.valueDumper(value: Any) {
    when (value) {
        is Gender -> append(value.name)
        is MyDate -> append("MyDate(${value.currentTimeMillis})")
    }
}

public val ContractSerializer: BinarySerializer = generatedBinarySerializer(::baseEncoders)
public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

public val MessageTransport: Transport = Transport(MessageSerializer, 100)
public val PacketTransport: Transport = Transport(PacketSerializer, 100)

public val Dumper: StringBuilder.(value: Any?) -> StringBuilder = dumper(::generatedDumperProperties, StringBuilder::valueDumper)

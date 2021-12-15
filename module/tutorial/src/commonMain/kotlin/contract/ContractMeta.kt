package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*
import ch.softappeal.yass2.tutorial.contract.generated.*

// This file describes the needed contract metadata.

/** Shows how to implement an own base type encoder. */
private val MyDateEncoder = BaseEncoder(MyDate::class,
    { writer, value -> writer.writeLong(value.currentTimeMillis) },
    { reader -> MyDate(reader.readLong()) }
)

/** Define all the base encoders needed by the contract (including enumerations and own base types). */
val BaseEncoders = listOf(
    IntEncoder,
    StringEncoder,
    enumEncoder<Gender>(),
    MyDateEncoder,
)

/** Define all the concrete classes needed by the contract. */
val ConcreteClasses = listOf(
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
)

val ContractSerializer = binarySerializer(BaseEncoders)
val MessageSerializer = binaryMessageSerializer(ContractSerializer)
val PacketSerializer = binaryPacketSerializer(MessageSerializer)

/** Define the [ServiceId] for each contract interface. */
val CalculatorId = serviceId<Calculator>(1)
val NewsListenerId = serviceId<NewsListener>(2)

/** Define all used [ServiceId]. */
val ServiceIds = listOf(CalculatorId, NewsListenerId)

/**
 * Define [ValueDumper] for base types.
 * [Boolean], [Number] and [CharSequence] are handled by default.
 */
val ValueDumper: ValueDumper = { value ->
    when (value) {
        is Gender -> append(value.name)
        is MyDate -> append("MyDate(${value.currentTimeMillis})")
    }
}

val MessageTransport = Transport(MessageSerializer, 100)
val PacketTransport = Transport(PacketSerializer, 100)

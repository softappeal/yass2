@file:BinarySerializerAndDumper(
    baseEncoderClasses = [
        // Define all the base encoders needed by the contract (including own base types).
        IntEncoder::class,
        StringEncoder::class,
        MyDateEncoder::class,
    ],
    treeConcreteClasses = [
        //  Define all the concrete classes (including enumerations) needed by the contract.
        Gender::class,
        Address::class,
        Person::class,
        DivideByZeroException::class,
        SubClass::class,
    ],
)

package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*

// This file describes the needed contract metadata.

// Shows how to implement an own base type encoder.
internal class MyDateEncoder : BaseEncoder<MyDate>(MyDate::class,
    { writer, value -> writer.writeLong(value.currentTimeMillis) },
    { reader -> MyDate(reader.readLong()) }
)

/** Define the [ServiceId] for each contract interface. */
@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId(1)

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId(2)

public val Dump: Dumper = createDumper { value ->
    // Writes value (without line breaks) if responsible else does nothing.
    when (value) {
        is MyDate -> append("MyDate(${value.currentTimeMillis})")
    }
}

public val ContractSerializer: Serializer = createSerializer()
public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

private const val INITIAL_WRITER_CAPACITY = 100
public val MessageTransport: Transport = Transport(MessageSerializer, INITIAL_WRITER_CAPACITY)
public val PacketTransport: Transport = Transport(PacketSerializer, INITIAL_WRITER_CAPACITY)

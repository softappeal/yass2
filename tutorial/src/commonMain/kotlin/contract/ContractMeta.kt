@file:GenerateBinarySerializer(
    baseEncoderClasses = [
        // Define all the base encoders needed by the contract (including enumerations and own base types).
        // [BooleanEncoder] is needed because [BooleanFlowId] returns a `Flow<Boolean>`.
        IntEncoder::class,
        BooleanEncoder::class,
        StringEncoder::class,
        GenderEncoder::class,
        MyDateEncoder::class,
    ],
    treeConcreteClasses = [
        //  Define all the concrete classes needed by the contract.
        Address::class,
        Person::class,
        DivideByZeroException::class,
        SubClass::class,
        BooleanFlowId::class,
        IntFlowId::class,
    ],
    graphConcreteClasses = [ // TODO: needed for js bug
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

internal class GenderEncoder : EnumEncoder<Gender>(Gender::class, enumValues())

/** Writes value (without line breaks) if responsible else does nothing. */
internal fun StringBuilder.valueDumper(value: Any) {
    when (value) {
        is MyDate -> append("MyDate(${value.currentTimeMillis})")
    }
}

/** Define the [ServiceId] for each contract interface. */
@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId(1)

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId(2)

/** Needed for [kotlinx.coroutines.flow.Flow] example. */
@MustBeImplementedByAcceptor
public val FlowServiceId: ServiceId<FlowService> = ServiceId(3)

public val Dumper: Dumper = dumper(GeneratedDumperProperties, StringBuilder::valueDumper)

public val MessageSerializer: Serializer = binaryMessageSerializer(GeneratedBinarySerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

private const val INITIAL_WRITER_CAPACITY = 100
public val MessageTransport: Transport = Transport(MessageSerializer, INITIAL_WRITER_CAPACITY)
public val PacketTransport: Transport = Transport(PacketSerializer, INITIAL_WRITER_CAPACITY)

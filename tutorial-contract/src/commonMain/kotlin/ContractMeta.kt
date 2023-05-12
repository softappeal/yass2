package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.binary.*

// This file describes the needed contract metadata.

// Shows how to implement an own base type encoder.
private val MyDateEncoder = BaseEncoder(MyDate::class,
    { writer, value -> writer.writeLong(value.currentTimeMillis) },
    { reader -> MyDate(reader.readLong()) }
)

/** Writes value (without line breaks) if responsible else does nothing. */
internal fun StringBuilder.valueDumper(value: Any) {
    when (value) {
        is MyDate -> append("MyDate(${value.currentTimeMillis})")
    }
}

/**
 * Define all the base encoders needed by the contract (including enumerations and own base types).
 * [BooleanEncoder] is needed because [BooleanFlowId] returns a `Flow<Boolean>`.
 */
internal val BaseEncoders = listOf(
    IntEncoder,
    BooleanEncoder,
    StringEncoder,
    enumEncoder<Gender>(),
    MyDateEncoder,
)

/** Define all the concrete classes needed by the contract. */
internal val ConcreteClasses = listOf(
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
    BooleanFlowId::class,
    IntFlowId::class,
)

/** Define the [ServiceId] for each contract interface. */
@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId(1)

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId(2)

/** Needed for [kotlinx.coroutines.flow.Flow] example. */
@MustBeImplementedByAcceptor
public val FlowServiceId: ServiceId<FlowService> = ServiceId(3)

internal val Services = listOf(Calculator::class, NewsListener::class)

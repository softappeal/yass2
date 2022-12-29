package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.binary.*

// This file describes the needed contract metadata.

@Suppress("PrivatePropertyName")
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

/** Define all the base encoders needed by the contract (including enumerations and own base types). */
internal val BaseEncoders = listOf(
    IntEncoder,
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
)

// Define the ServiceId for each contract interface.

@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = serviceId(1)

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = serviceId(2)

/** Define all used [ServiceId]. */
internal val ServiceIds = listOf(CalculatorId, NewsListenerId)

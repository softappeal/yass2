package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.binary.*

// This file describes the needed contract metadata.

/** Define all the base encoders needed by the contract (including enumerations and own base types). */
fun baseEncoders(): List<BaseEncoder<out Any>> {
    // Shows how to implement an own base type encoder.
    val myDateEncoder = BaseEncoder(MyDate::class,
        { writer, value -> writer.writeLong(value.currentTimeMillis) },
        { reader -> MyDate(reader.readLong()) }
    )
    return listOf(
        IntEncoder,
        StringEncoder,
        enumEncoder<Gender>(),
        myDateEncoder,
    )
}

/** Define all the concrete classes needed by the contract. */
val ConcreteClasses = listOf(
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
)

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

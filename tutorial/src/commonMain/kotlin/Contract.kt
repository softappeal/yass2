package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.coroutines.session.MustBeImplementedByAcceptor
import ch.softappeal.yass2.remote.coroutines.session.MustBeImplementedByInitiator
import ch.softappeal.yass2.serialize.binary.BaseEncoder
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.serialize.binary.StringEncoder
import ch.softappeal.yass2.serialize.binary.readLong
import ch.softappeal.yass2.serialize.binary.writeLong

// This file describes the contract (data transfer objects and interfaces) between client and server.

/**
 * The base types Boolean, Byte, Int, Long, Double, String and ByteArray are supported.
 * Other own base types like [MyDate] could be added.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
public class MyDate(public val currentTimeMillis: Long)

// Shows how to implement an own base type encoder.
private class MyDateEncoder : BaseEncoder<MyDate>(MyDate::class,
    { writer, value -> writer.writeLong(value.currentTimeMillis) },
    { reader -> MyDate(reader.readLong()) }
)

private fun Appendable.append(value: MyDate) {
    append("MyDate(${value.currentTimeMillis})")
}

/**
 * A concrete class must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Properties can be optional.
 */
public class Address(
    public val street: String,
) {
    public var number: Int? = null
}

/** Enumerations are supported. */
public enum class Gender {
    Female,
    Male,
}

private class GenderEncoder : EnumEncoder<Gender>(Gender::class, enumValues()) // TODO: remove

/** Lists are supported. */
public class Person(
    public val name: String,
    public val gender: Gender,
    public val birthday: MyDate,
    public val addresses: List<Address>,
)

/**
 * Exceptions are supported.
 * They are basically like regular classes but [Throwable.message] and [Throwable.cause] aren't serialized.
 */
public class DivideByZeroException : RuntimeException()

/**
 * Inheritance is supported.
 * Base class properties must be in body and abstract and overridden in subclasses.
 */
public abstract class BaseClass {
    public abstract val baseClassProperty: String
}

public class SubClass(
    public override val baseClassProperty: String,
    public val subClassProperty: String,
) : BaseClass()

/**
 * All functions must be suspendable because they need IO.
 * Overloading is not allowed.
 */
public interface Calculator {
    public suspend fun add(a: Int, b: Int): Int
    public suspend fun divide(a: Int, b: Int): Int
}

public interface NewsListener {
    public suspend fun notify(news: String)
}

@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId(1)

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId(2)

/** Define all the base encoders needed by the contract (including enumerations and own base types). */
internal val BaseEncoders = listOf(
    IntEncoder(),
    StringEncoder(),
    MyDateEncoder(),
    GenderEncoder(),
)

internal val TreeConcreteClasses = listOf(
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
)

internal val Services = listOf(
    Calculator::class,
    NewsListener::class,
)

internal fun Appendable.dumpValue(value: Any) {
    // Writes value (without line breaks) if responsible else does nothing.
    when (value) {
        is MyDate -> append(value)
    }
}

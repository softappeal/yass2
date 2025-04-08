package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.ValueReply
import ch.softappeal.yass2.core.serialize.string.BaseStringEncoder
import ch.softappeal.yass2.core.serialize.string.IntStringEncoder
import ch.softappeal.yass2.core.serialize.string.StringSerializer
import ch.softappeal.yass2.core.serialize.string.TextSerializer
import ch.softappeal.yass2.coroutines.session.MustBeImplementedByAcceptor
import ch.softappeal.yass2.coroutines.session.MustBeImplementedByInitiator
import ch.softappeal.yass2.coroutines.session.Packet

/**
 * Shows how to implement an own base type.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
public data class MyDate(public val currentTimeMillis: Long)
internal object MyDateEncoder : BaseStringEncoder<MyDate>(MyDate::class,
    { value -> value.currentTimeMillis.toString() },
    { MyDate(toLong()) }
)

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

internal val Services = listOf(Calculator::class, NewsListener::class)

// Define all the additional base encoders needed by the contract (including own base types and types used in services).
internal val EncoderObjects = listOf(
    // String and Boolean is built-in
    IntStringEncoder::class,
    MyDateEncoder::class,
)

internal val ConcreteClasses = listOf(
    Gender::class,
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
    Request::class, ValueReply::class, ExceptionReply::class, // needed by ch.softappeal.yass2.remote (also needs String)
    Packet::class, // needed by ch.softappeal.yass2.remote.coroutines (also needs Int)
)

@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId("calc")

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId("news")

public val TutorialSerializer: StringSerializer = TextSerializer(createStringEncoders())

package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.remote.coroutines.MustBeImplementedByAcceptor
import ch.softappeal.yass2.remote.coroutines.MustBeImplementedByInitiator
import ch.softappeal.yass2.remote.coroutines.Packet
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Transport
import ch.softappeal.yass2.serialize.utf8.IntUtf8Encoder
import ch.softappeal.yass2.serialize.utf8.TextSerializer
import ch.softappeal.yass2.serialize.utf8.Utf8Encoder

/**
 * Shows how to implement an own base type.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
public data class MyDate(public val currentTimeMillis: Long)
internal object MyDateEncoder : Utf8Encoder<MyDate>(MyDate::class,
    { value -> writeString(value.currentTimeMillis.toString()) },
    { MyDate(readString().toLong()) }
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

@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId("calc")

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId("news")

internal val ConcreteClasses = listOf(
    Gender::class,
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
    Request::class, ValueReply::class, ExceptionReply::class, // needed by ch.softappeal.yass2.remote (also needs String)
    Packet::class, // needed by ch.softappeal.yass2.remote.coroutines (also needs Int)
)

// Define all the additional base encoders needed by the contract (including own base types).
internal val EncoderObjects = listOf(
    // String is built-in
    IntUtf8Encoder::class,
    MyDateEncoder::class,
)

public val TransportSerializer: Serializer = TextSerializer(createUtf8Encoders(), true)

public val ContractTransport: Transport = Transport(TransportSerializer)

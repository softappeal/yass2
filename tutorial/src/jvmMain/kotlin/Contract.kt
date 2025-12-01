package tutorial

import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.remote.ExceptionReply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.ValueReply
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.string.BaseStringEncoder
import ch.softappeal.yass2.core.serialize.string.IntStringEncoder
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.core.serialize.string.TextSerializer
import ch.softappeal.yass2.coroutines.session.MustBeImplementedByAcceptor
import ch.softappeal.yass2.coroutines.session.MustBeImplementedByInitiator
import ch.softappeal.yass2.coroutines.session.Packet

/**
 * Shows how to implement an own base type.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
data class MyDate(val currentTimeMillis: Long)
internal object MyDateEncoder : BaseStringEncoder<MyDate>(
    MyDate::class,
    { value -> value.currentTimeMillis.toString() },
    { MyDate(toLong()) }
)

/**
 * A concrete class must have a primary constructor and all its parameters must be properties.
 * Properties can be optional.
 */
class Address(
    val street: String,
    var number: Int?,
)

/** Enumerations are supported. */
enum class Gender {
    Female,
    Male,
}

/** Lists are supported. */
class Person(
    val name: String,
    val gender: Gender,
    val birthday: MyDate,
    val addresses: List<Address>,
)

/**
 * Exceptions are supported.
 * They are basically like regular classes but [Throwable.message] and [Throwable.cause] aren't serialized.
 */
class DivideByZeroException : RuntimeException()

/**
 * Inheritance is supported.
 * Base class properties must be in body and abstract and overridden in subclasses.
 */
abstract class BaseClass {
    abstract val baseClassProperty: String
}

class SubClass(
    override val baseClassProperty: String,
    val subClassProperty: String,
) : BaseClass()

/**
 * All functions must be suspendable because they need IO.
 * Overloading is not allowed.
 */
interface Calculator {
    suspend fun add(a: Int, b: Int): Int
    suspend fun divide(a: Int, b: Int): Int
}

interface NewsListener {
    suspend fun notify(news: String)
}

// The following annotations generate the proxies and serializers.
// They can be added to anything in the package.

@Proxies(
    Calculator::class,
    NewsListener::class,
)
// Define all the additional base encoders needed by the contract (including own base types and types used in services).
@StringEncoderObjects(
    // String and Boolean is built-in
    IntStringEncoder::class,
    MyDateEncoder::class,
)
@ConcreteAndEnumClasses(
    Gender::class,
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
    Request::class, ValueReply::class, ExceptionReply::class, // needed by ch.softappeal.yass2.core.remote (also needs String)
    Packet::class, // needed by ch.softappeal.yass2.coroutines.session (also needs Int)
)
val TutorialSerializer = TextSerializer(StringEncoders)

@MustBeImplementedByAcceptor
val CalculatorId = ServiceId<Calculator>("calc")

@MustBeImplementedByInitiator
val NewsListenerId = ServiceId<NewsListener>("news")

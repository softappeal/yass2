package tutorial

import ch.softappeal.yass2.remote.ContractException
import ch.softappeal.yass2.remote.ExceptionReply
import ch.softappeal.yass2.remote.Proxies
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.ValueReply
import ch.softappeal.yass2.remote.session.MustBeImplementedByAcceptor
import ch.softappeal.yass2.remote.session.MustBeImplementedByInitiator
import ch.softappeal.yass2.remote.session.Packet
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.serialize.string.BaseStringEncoder
import ch.softappeal.yass2.serialize.string.IntStringEncoder
import ch.softappeal.yass2.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.serialize.string.TextSerializer

/**
 * Shows how to implement an own base type.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
data class MyDate(val currentTimeMillis: Long)
internal object MyDateEncoder : BaseStringEncoder<MyDate>(
    MyDate::class,
    { currentTimeMillis.toString() },
    { MyDate(toLong()) },
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
 * Exceptions are supported. They must be [ContractException]s.
 * They are basically like regular classes but [Throwable.message] and [Throwable.cause] aren't serialized.
 */
class DivideByZeroException : ContractException()

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

val ContractSerializer = TextSerializer(stringEncoders())

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

@Proxies(
    Calculator::class,
    NewsListener::class,
)

// Define all the encoders needed by the contract.
@StringEncoderObjects(
    // String and Boolean is built-in
    IntStringEncoder::class,
    MyDateEncoder::class,
)

@ConcreteAndEnumClasses(
    Packet::class, // needed by ch.softappeal.yass2.coroutines.session (also needs Int)
    Request::class, ValueReply::class, ExceptionReply::class, // needed by ch.softappeal.yass2.core.remote (also needs String)
    Gender::class,
    Address::class,
    Person::class,
    DivideByZeroException::class,
    SubClass::class,
)

@MustBeImplementedByAcceptor
val CalculatorId = ServiceId<Calculator>("Calculator")

@MustBeImplementedByInitiator
val NewsListenerId = ServiceId<NewsListener>("NewsListener")

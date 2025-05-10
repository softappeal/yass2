package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.core.Proxy
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.string.BaseStringEncoder
import ch.softappeal.yass2.core.serialize.string.IntStringEncoder
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.core.serialize.string.TextSerializer

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
 * Body properties are allowed but must be of `var` kind.
 * Properties can be optional.
 */
class Address(
    val street: String,
) {
    var number: Int? = null
}

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
@Proxy
interface Calculator {
    suspend fun add(a: Int, b: Int): Int
    suspend fun divide(a: Int, b: Int): Int
}

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
)
@Suppress("unused") private class Generate

val TutorialSerializer = TextSerializer(createStringEncoders())

val CalculatorId = ServiceId<Calculator>("calc")

package ch.softappeal.yass2.tutorial.contract

// This file describes the contract (data transfer objects and interfaces) between client and server.
// It is plain Kotlin code without dependencies and can therefore be shared with every platform (common module).

/**
 * The base types Boolean, Byte, Int, Long, Double, String and ByteArray are supported.
 * Other own base types like [MyDate] could be added.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
class MyDate(val currentTimeMillis: Long)

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

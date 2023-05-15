package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.*

// This file describes the contract (data transfer objects and interfaces) between client and server.

/**
 * The base types Boolean, Byte, Int, Long, Double, String and ByteArray are supported.
 * Other own base types like [MyDate] could be added.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
public class MyDate(public val currentTimeMillis: Long)

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

/** Needed for [kotlinx.coroutines.flow.Flow] example. */
public sealed class FlowId
@Suppress("CanSealedSubClassBeObject") public class BooleanFlowId : FlowId()
public class IntFlowId(public val max: Int) : FlowId()

/**
 * All functions must be suspendable because they need IO.
 * Overloading is not allowed.
 */
@Proxy
public interface Calculator {
    public suspend fun add(a: Int, b: Int): Int
    public suspend fun divide(a: Int, b: Int): Int
}

@Proxy
public interface NewsListener {
    public suspend fun notify(news: String)
}

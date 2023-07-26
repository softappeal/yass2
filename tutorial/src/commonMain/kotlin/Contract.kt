@file:GenerateBinarySerializer(
    baseEncoderClasses = [
        // Define all the base encoders needed by the contract (including own base types).
        IntEncoder::class,
        StringEncoder::class,
        MyDateEncoder::class,
    ],
    treeConcreteClasses = [
        //  Define all the concrete classes (including enumerations) needed by the contract.
        Gender::class,
        Address::class,
        Person::class,
        DivideByZeroException::class,
        SubClass::class,
    ],
    graphConcreteClasses = [],
    withDumper = true,
)

package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.*
import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.remote.coroutines.session.*
import ch.softappeal.yass2.serialize.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.transport.*
import ch.softappeal.yass2.transport.session.*

// This file describes the contract (data transfer objects and interfaces) between client and server.

/**
 * The base types Boolean, Byte, Int, Long, Double, String and ByteArray are supported.
 * Other own base types like [MyDate] could be added.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
public class MyDate(public val currentTimeMillis: Long)

// Shows how to implement an own base type encoder.
internal class MyDateEncoder : BaseEncoder<MyDate>(MyDate::class,
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
@GenerateProxy
public interface Calculator {
    public suspend fun add(a: Int, b: Int): Int
    public suspend fun divide(a: Int, b: Int): Int
}

@GenerateProxy
public interface NewsListener {
    public suspend fun notify(news: String)
}

/** Define the [ServiceId] for each contract interface. */

@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId(1)

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId(2)

public val Dump: Dumper = createDumper { value ->
    // Writes value (without line breaks) if responsible else does nothing.
    when (value) {
        is MyDate -> append(value)
    }
}

public val ContractSerializer: Serializer = createSerializer()
public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

private const val INITIAL_WRITER_CAPACITY = 100
public val MessageTransport: Transport = Transport(MessageSerializer, INITIAL_WRITER_CAPACITY)
public val PacketTransport: Transport = Transport(PacketSerializer, INITIAL_WRITER_CAPACITY)

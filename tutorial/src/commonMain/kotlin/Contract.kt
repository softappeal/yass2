package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.binaryMessageSerializer
import ch.softappeal.yass2.remote.coroutines.MustBeImplementedByAcceptor
import ch.softappeal.yass2.remote.coroutines.MustBeImplementedByInitiator
import ch.softappeal.yass2.remote.coroutines.binaryPacketSerializer
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Transport
import ch.softappeal.yass2.serialize.binary.Encoder
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.serialize.binary.StringEncoder
import ch.softappeal.yass2.serialize.binary.readLong
import ch.softappeal.yass2.serialize.binary.writeLong

/**
 * The base types Boolean, Byte, Int, Long, Double, String and ByteArray are supported.
 * Other own base types like [MyDate] could be added.
 * In contrast to regular classes, own base types could implement a more efficient serializing.
 */
public class MyDate(public val currentTimeMillis: Long)

// Shows how to implement an own base type encoder.
internal class MyDateEncoder : Encoder<MyDate>(MyDate::class,
    { value -> writeLong(value.currentTimeMillis) },
    { MyDate(readLong()) }
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

@MustBeImplementedByAcceptor
public val CalculatorId: ServiceId<Calculator> = ServiceId(1)

@MustBeImplementedByInitiator
public val NewsListenerId: ServiceId<NewsListener> = ServiceId(2)

@GenerateBinarySerializer(
    baseEncoderClasses = [
        // Define all the base encoders needed by the contract (including own base types).
        IntEncoder::class,
        StringEncoder::class,
        MyDateEncoder::class,
    ],
    enumClasses = [
        Gender::class,
    ],
    concreteClasses = [
        Address::class,
        Person::class,
        DivideByZeroException::class,
        SubClass::class,
    ],
)
public val ContractSerializer: Serializer = createBinarySerializer()

public val MessageSerializer: Serializer = binaryMessageSerializer(ContractSerializer)
public val PacketSerializer: Serializer = binaryPacketSerializer(MessageSerializer)

public val MessageTransport: Transport = Transport(MessageSerializer)
public val PacketTransport: Transport = Transport(PacketSerializer)

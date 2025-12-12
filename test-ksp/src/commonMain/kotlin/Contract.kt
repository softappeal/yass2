@file:Suppress("unused")

package test

import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.core.serialize.string.IntStringEncoder
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.core.serialize.string.TextSerializer

interface Calculator {
    suspend fun add(a: Int, b: Int): Int
}

@Proxies(
    Calculator::class,
)

@BinaryEncoderObjects(
    IntBinaryEncoder::class,
    StringBinaryEncoder::class,
)
@StringEncoderObjects(
    IntStringEncoder::class,
)
@ConcreteAndEnumClasses(
    Request::class, // see generate/ksp/GenerateSerializer.kt: .filter { it.isPublic() }
)
internal object Generate

val BinarySerializer = binarySerializer()
val TextSerializer = TextSerializer(stringEncoders())

object CalculatorImpl : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
}

val CalculatorProxy = CalculatorImpl.proxy { _, _, invoke -> invoke() }

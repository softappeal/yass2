package test

import ch.softappeal.yass2.Proxies
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.serialize.binary.IntBinaryEncoder
import ch.softappeal.yass2.serialize.binary.StringBinaryEncoder
import ch.softappeal.yass2.serialize.string.IntStringEncoder
import ch.softappeal.yass2.serialize.string.StringEncoderObjects

enum class Gender { Female, Male }

class Types(
    val int: Int,
    val gender: Gender,
)

interface Calculator {
    suspend fun divide(a: Int, b: Int): Int
}

@Proxies(
    Calculator::class,
)

@ConcreteAndEnumClasses(
    Gender::class,
    Types::class,
    Request::class, // see /src/jvmMain/kotlin/generate/ksp/GenerateSerializer.kt: .filter { it.isPublic() }
)
@BinaryEncoderObjects(
    IntBinaryEncoder::class,
    StringBinaryEncoder::class,
)
@StringEncoderObjects(
    IntStringEncoder::class,
)
internal object Generate

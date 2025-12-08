package test

import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.binary.StringBinaryEncoder

@ConcreteAndEnumClasses(
    Request::class, // see generate/ksp/GenerateSerializer.kt: .filter { it.isPublic() }
)
@BinaryEncoderObjects(
    StringBinaryEncoder::class,
)
internal object Generate

package ch.softappeal.yass2.core.serialize.string

import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

public object IntStringEncoder : BaseStringEncoder<Int>(
    Int::class,
    { value -> value.toString() },
    { toInt() }
)

private val B64 = Base64.Default // uses A-Za-z0-9+/=

public object ByteArrayStringEncoder : BaseStringEncoder<ByteArray>(
    ByteArray::class,
    { value -> B64.encode(value) },
    { B64.decode(this) }
)

public class EnumStringEncoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : BaseStringEncoder<T>(
    type,
    { value -> value.toString() },
    { valueOf(this) }
)

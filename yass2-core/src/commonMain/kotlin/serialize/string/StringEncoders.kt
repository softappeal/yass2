package ch.softappeal.yass2.core.serialize.string

import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

public object IntStringEncoder : BaseStringEncoder<Int>(
    Int::class,
    { toString() },
    { toInt() }
)

private val B64 = Base64.Default // uses A-Za-z0-9+/=

public object ByteArrayStringEncoder : BaseStringEncoder<ByteArray>(
    ByteArray::class,
    { B64.encode(this) },
    { B64.decode(this) }
)

public class EnumStringEncoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : BaseStringEncoder<T>(
    type,
    { toString() },
    { valueOf(this) }
)

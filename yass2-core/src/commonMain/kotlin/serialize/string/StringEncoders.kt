package ch.softappeal.yass2.core.serialize.string

import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

public object IntStringEncoder : BaseStringEncoder<Int>(
    Int::class,
    { value -> value.toString() },
    { toInt() }
)

public object LongStringEncoder : BaseStringEncoder<Long>(
    Long::class,
    { value -> value.toString() },
    { toLong() }
)

/** NOTE: Doesn't work for [whole numbers on the JS platform](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger). */
public object DoubleStringEncoder : BaseStringEncoder<Double>(
    Double::class,
    { value -> value.toString() },
    { toDouble() }
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

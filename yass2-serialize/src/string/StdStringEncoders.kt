package ch.softappeal.yass2.serialize.string

import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

public object IntStringEncoder : BaseStringEncoder<Int>(
    Int::class,
    { toString() },
    { toInt() },
)

public object LongStringEncoder : BaseStringEncoder<Long>(
    Long::class,
    { toString() },
    { toLong() }
)

/** NOTE: Doesn't work for [whole numbers on the JS platform](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger). */
public object DoubleStringEncoder : BaseStringEncoder<Double>(
    Double::class,
    { toString() },
    { toDouble() }
)

private val B64 = Base64.Default // uses A-Za-z0-9+/=

public object ByteArrayStringEncoder : BaseStringEncoder<ByteArray>(
    ByteArray::class,
    { B64.encode(this) },
    { B64.decode(this) },
)

public class EnumStringEncoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : BaseStringEncoder<T>(
    type,
    { toString() },
    { valueOf(this) },
)

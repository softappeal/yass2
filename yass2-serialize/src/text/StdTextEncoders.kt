package ch.softappeal.yass2.serialize.text

import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

public object BooleanTextEncoder : TextEncoder<Boolean>(
    Boolean::class,
    { toString() },
    { toBooleanStrict() },
)

public object IntTextEncoder : TextEncoder<Int>(
    Int::class,
    { toString() },
    { toInt() },
)

public object LongTextEncoder : TextEncoder<Long>(
    Long::class,
    { toString() },
    { toLong() }
)

/** NOTE: [DoubleTextEncoder] doesn't work for [whole numbers on the JS platform](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger). */
public object DoubleTextEncoder : TextEncoder<Double>(
    Double::class,
    { toString() },
    { toDouble() }
)

private val B64 = Base64.Default // uses A-Za-z0-9+/=

public object ByteArrayTextEncoder : TextEncoder<ByteArray>(
    ByteArray::class,
    { B64.encode(this) },
    { B64.decode(this) },
)

public class EnumTextEncoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : TextEncoder<T>(
    type,
    { toString() },
    { valueOf(this) },
)

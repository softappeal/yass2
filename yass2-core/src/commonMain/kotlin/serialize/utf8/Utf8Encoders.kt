package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.NotJs
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass

public object BooleanUtf8Encoder : BaseUtf8Encoder<Boolean>(Boolean::class,
    { value -> value.toString() },
    { toBooleanStrict() }
)

public object IntUtf8Encoder : BaseUtf8Encoder<Int>(Int::class,
    { value -> value.toString() },
    { toInt() }
)

public object LongUtf8Encoder : BaseUtf8Encoder<Long>(Long::class,
    { value -> value.toString() },
    { toLong() }
)

// Doesn't work for whole numbers on js target: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger
@NotJs
public object DoubleUtf8Encoder : BaseUtf8Encoder<Double>(Double::class,
    { value -> value.toString() },
    { toDouble() }
)

@ExperimentalEncodingApi
private val B64 = Base64.Default // uses A-Za-z0-9+/=

@OptIn(ExperimentalEncodingApi::class)
public object ByteArrayUtf8Encoder : BaseUtf8Encoder<ByteArray>(ByteArray::class,
    { value -> B64.encode(value) },
    { B64.decode(this) }
)

public class EnumUtf8Encoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : BaseUtf8Encoder<T>(type,
    { value -> value.toString() },
    { valueOf(this) }
)

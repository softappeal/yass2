package ch.softappeal.yass2.serialize.string

import ch.softappeal.yass2.NotJs
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass

public object BooleanStringEncoder : BaseStringEncoder<Boolean>(Boolean::class,
    { value -> value.toString() },
    { toBooleanStrict() }
)

public fun Boolean(string: String): Boolean = BooleanStringEncoder.read(string)

public object IntStringEncoder : BaseStringEncoder<Int>(Int::class,
    { value -> value.toString() },
    { toInt() }
)

public fun Int(string: String): Int = IntStringEncoder.read(string)

public object LongStringEncoder : BaseStringEncoder<Long>(Long::class,
    { value -> value.toString() },
    { toLong() }
)

public fun Long(string: String): Long = LongStringEncoder.read(string)

// Doesn't work for whole numbers on js target: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isInteger
@NotJs
public object DoubleStringEncoder : BaseStringEncoder<Double>(Double::class,
    { value -> value.toString() },
    { toDouble() }
)

@NotJs
public fun Double(string: String): Double = DoubleStringEncoder.read(string)

@ExperimentalEncodingApi
private val B64 = Base64.Default // uses A-Za-z0-9+/=

@OptIn(ExperimentalEncodingApi::class)
public object ByteArrayStringEncoder : BaseStringEncoder<ByteArray>(ByteArray::class,
    { value -> B64.encode(value) },
    { B64.decode(this) }
)

public fun ByteArray(string: String): ByteArray = ByteArrayStringEncoder.read(string)

public class EnumStringEncoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : BaseStringEncoder<T>(type,
    { value -> value.toString() },
    { valueOf(this) }
)

public operator fun <E : Enum<E>> E.invoke(): E = this

public operator fun Nothing?.invoke(): Nothing? = null

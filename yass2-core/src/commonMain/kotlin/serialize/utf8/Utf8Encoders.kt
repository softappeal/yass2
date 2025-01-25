package ch.softappeal.yass2.serialize.utf8

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass

public object BooleanUtf8Encoder : Utf8Encoder<Boolean>(Boolean::class,
    { value -> writeString(value.toString()) },
    { readString().toBoolean() }
)

public object IntUtf8Encoder : Utf8Encoder<Int>(Int::class,
    { value -> writeString(value.toString()) },
    { readString().toInt() }
)

public object LongUtf8Encoder : Utf8Encoder<Long>(Long::class,
    { value -> writeString(value.toString()) },
    { readString().toLong() }
)

public object DoubleUtf8Encoder : Utf8Encoder<Double>(Double::class,
    { value -> writeString(value.toString()) },
    { readString().toDouble() }
)

@ExperimentalEncodingApi
private val B64 = Base64.Default // uses A-Za-z0-9+/=

@OptIn(ExperimentalEncodingApi::class)
public object ByteArrayUtf8Encoder : Utf8Encoder<ByteArray>(ByteArray::class,
    { value -> writeString(B64.encode(value)) },
    { B64.decode(readString()) }
)

public class EnumUtf8Encoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : Utf8Encoder<T>(type,
    { value -> writeString(value.toString()) },
    { valueOf(readString()) }
)

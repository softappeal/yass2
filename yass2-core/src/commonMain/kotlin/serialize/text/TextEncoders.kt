package ch.softappeal.yass2.serialize.text

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass

public class BooleanTextEncoder : TextEncoder<Boolean>(Boolean::class,
    { value -> writeTextBytes(value.toString()) },
    { readTextBytes().toBoolean() }
)

public class IntTextEncoder : TextEncoder<Int>(Int::class,
    { value -> writeTextBytes(value.toString()) },
    { readTextBytes().toInt() }
)

public class LongTextEncoder : TextEncoder<Long>(Long::class,
    { value -> writeTextBytes(value.toString()) },
    { readTextBytes().toLong() }
)

public class DoubleTextEncoder : TextEncoder<Double>(Double::class,
    { value -> writeTextBytes(value.toString()) },
    { readTextBytes().toDouble() }
)

@ExperimentalEncodingApi
private val B64 = Base64.Default

@OptIn(ExperimentalEncodingApi::class)
public class ByteArrayTextEncoder : TextEncoder<ByteArray>(ByteArray::class,
    { value -> writeTextBytes(B64.encode(value)) },
    { B64.decode(readTextBytes()) }
)

public class EnumTextEncoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : TextEncoder<T>(type,
    { value -> writeTextBytes(value.toString()) },
    { valueOf(readTextBytes()) }
)

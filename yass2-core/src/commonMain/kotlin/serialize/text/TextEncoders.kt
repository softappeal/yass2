package ch.softappeal.yass2.serialize.text // TODO: add other encoders

import kotlin.reflect.KClass

public class IntTextEncoder : TextEncoder<Int>(Int::class,
    { value -> writeTextBytes(value.toString()) },
    { readTextBytes().toInt() }
)

public class EnumTextEncoder<T : Enum<T>>(type: KClass<T>, valueOf: (name: String) -> T) : TextEncoder<T>(type,
    { value -> writeTextBytes(value.toString()) },
    { valueOf(readTextBytes()) }
)

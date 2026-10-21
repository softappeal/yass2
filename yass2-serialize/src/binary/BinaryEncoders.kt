package ch.softappeal.yass2.serialize.binary

import kotlin.reflect.KClass

public annotation class BinaryEncoders(vararg val value: KClass<out BinaryEncoder<*>>)


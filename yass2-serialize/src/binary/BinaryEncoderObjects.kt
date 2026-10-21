package ch.softappeal.yass2.serialize.binary

import kotlin.reflect.KClass

public annotation class BinaryEncoderObjects(vararg val value: KClass<out BinaryEncoder<*>>)


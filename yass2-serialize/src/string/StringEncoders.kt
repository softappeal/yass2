package ch.softappeal.yass2.serialize.string

import kotlin.reflect.KClass

public annotation class StringEncoders(vararg val value: KClass<out BaseStringEncoder<*>>)

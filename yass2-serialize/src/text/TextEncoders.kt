package ch.softappeal.yass2.serialize.text

import kotlin.reflect.KClass

public annotation class TextEncoders(vararg val value: KClass<out TextEncoder<*>>)

package ch.softappeal.yass2.serialize

import kotlin.reflect.KClass

public annotation class ConcreteAndEnumClasses(vararg val value: KClass<*>)

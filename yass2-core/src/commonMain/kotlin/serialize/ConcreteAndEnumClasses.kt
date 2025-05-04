package ch.softappeal.yass2.core.serialize

import kotlin.reflect.KClass

/**
 * Concrete classes must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 */
@Target(AnnotationTarget.CLASS)
public annotation class ConcreteAndEnumClasses(vararg val value: KClass<*>)

package ch.softappeal.yass2

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*

@Target(AnnotationTarget.CLASS)
public annotation class GenerateProxy

@Target(AnnotationTarget.FILE)
public annotation class GenerateBinarySerializerAndDumper(
    val baseEncoderClasses: Array<KClass<out BaseEncoder<*>>>,
    val treeConcreteClasses: Array<KClass<*>>,
    val graphConcreteClasses: Array<KClass<*>> = [],
)

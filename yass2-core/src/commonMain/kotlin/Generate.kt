package ch.softappeal.yass2 // TODO

import kotlin.reflect.*

@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class ServiceWithId(
    val service: KClass<*>,
    val id: Int,
)

@Target(AnnotationTarget.FILE)
public annotation class Generate(
    val list: Array<ServiceWithId>,
)

// @Target(AnnotationTarget.CLASS)
//public annotation class Generate // flags interfaces and classes for generating metadata for remote and dumper and binarySerializer

/*

@Target(AnnotationTarget.FILE)
public annotation class GenerateBinarySerializer(
    val baseEncoders
    val treeConcreteClasses: Array<KClass<*>>,
    val graphConcreteClasses: Array<KClass<*>> = [],
)

@Target(AnnotationTarget.FILE)
public annotation class GenerateBinarySerializerAndDumper(
    val treeConcreteClasses: Array<KClass<*>>,
    val graphConcreteClasses: Array<KClass<*>> = [],
)

@Target(AnnotationTarget.FILE)
public annotation class GenerateDumper(
    val concreteClasses: Array<KClass<*>>,
)

 */

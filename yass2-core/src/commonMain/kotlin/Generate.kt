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

// infer package

// make Proxy instead of ProxyFactory, move into matching package

// generate service Ids
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

@Target(AnnotationTarget.FILE)
public annotation class GenerateProxyFactory(
    val services: Array<KClass<*>>,
)

@Target(AnnotationTarget.FILE)
public annotation class GenerateRemote(
    serviceIds: List<ServiceId<*>>,
)

// and both of above!

 */

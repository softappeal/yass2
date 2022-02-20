package ch.softappeal.yass2.serialize.binary.reflect

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*
import kotlin.reflect.full.*

public fun KClass<*>.metaClass(baseEncoderTypes: List<KClass<*>>, concreteClasses: List<KClass<*>>): MetaClass {
    require(!java.isEnum) { "type '$this' is enum" }
    require(!isAbstract) { "type '$this' is abstract" }
    return MetaClass(
        this,
        memberProperties
            .filter { !isSubclassOf(Throwable::class) || (it.name != "cause" && it.name != "message") }
            .sortedBy { it.name }
            .map { property ->
                (property.returnType.classifier as KClass<*>).metaProperty(
                    @Suppress("UNCHECKED_CAST") (property as KProperty1<Any, Any?>),
                    baseEncoderTypes,
                    concreteClasses,
                    property.returnType.isMarkedNullable,
                ) { it != this && isSubclassOf(it) }
            },
        (primaryConstructor ?: error("'$this' has no primary constructor")).valueParameters.map { it.name!! }
    )
}

public fun reflectionBinarySerializer(
    baseEncodersSupplier: () -> List<BaseEncoder<*>>, // NOTE: supplier is needed due to Kotlin Native/JS global variables initialize order bug
    concreteClasses: List<KClass<*>> = emptyList(),
): BinarySerializer {
    val baseEncoders = baseEncodersSupplier()
    val baseEncoderTypes = baseEncoders.map { it.type }
    val encoders = mutableListOf<Encoder>()
    encoders.addAll(baseEncoders)
    concreteClasses.forEach { klass ->
        val metaClass = klass.metaClass(baseEncoderTypes, concreteClasses)
        encoders.add(ClassEncoder(@Suppress("UNCHECKED_CAST") (klass as KClass<Any>),
            { writer, instance ->
                metaClass.properties.forEach { it.write(writer, it.property.get(instance)) }
            },
            { reader ->
                val parameterProperties = Array(metaClass.parameterProperties.size) { metaClass.parameterProperties[it].read(reader) }
                val instance = klass.primaryConstructor!!.call(*parameterProperties)
                metaClass.bodyProperties.forEach { it.mutableProperty().set(instance, it.read(reader)) }
                instance
            }
        ))
    }
    return BinarySerializer(encoders)
}

package ch.softappeal.yass2.serialize.binary

import kotlin.reflect.*
import kotlin.reflect.full.*

internal fun KClass<*>.metaClass(baseEncoderTypes: List<KClass<*>>, concreteClasses: List<KClass<*>>): MetaClass {
    require(!java.isEnum) { "type '$this' is enum" }
    require(!isAbstract) { "type '$this' is abstract" }
    return MetaClass(
        this,
        memberProperties
            .filter { !isSubclassOf(Throwable::class) || (it.name != "cause" && it.name != "message") }
            .sortedBy { it.name }
            .map { property ->
                @Suppress("UNCHECKED_CAST") (property.returnType.classifier as KClass<*>).metaProperty(
                    property as KProperty1<Any, Any?>, baseEncoderTypes, concreteClasses, property.returnType.isMarkedNullable
                ) { it != this && isSubclassOf(it) }
            },
        (primaryConstructor ?: error("'$this' has no primary constructor")).valueParameters.map { it.name!! }
    )
}

fun reflectionBinarySerializer(baseEncoders: List<BaseEncoder<*>>, concreteClasses: List<KClass<*>>): BinarySerializer {
    val baseEncoderTypes = baseEncoders.map { it.type }
    val encoders = mutableListOf<Encoder>()
    encoders.addAll(baseEncoders)
    concreteClasses.forEach { klass ->
        val metaClass = klass.metaClass(baseEncoderTypes, concreteClasses)
        @Suppress("UNCHECKED_CAST") encoders.add(ClassEncoder(klass as KClass<Any>,
            { writer, instance ->
                metaClass.properties.forEach { it.write(writer, it.property.get(instance)) }
            },
            { reader ->
                val properties = Array(metaClass.properties.size) { metaClass.properties[it].read(reader) }
                val instance = klass.primaryConstructor!!.call(*Array(metaClass.parameterIndices.size) {
                    properties[metaClass.parameterIndices[it]]
                })
                metaClass.varIndices.forEach { metaClass.properties[it].mutableProperty().set(instance, properties[it]) }
                instance
            }
        ))
    }
    return BinarySerializer(encoders)
}

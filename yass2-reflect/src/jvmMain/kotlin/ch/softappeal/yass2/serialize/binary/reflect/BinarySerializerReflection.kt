package ch.softappeal.yass2.serialize.binary.reflect

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*
import kotlin.reflect.full.*

public fun KClass<*>.metaClass(baseEncoderTypes: List<KClass<*>>): MetaClass {
    require(!java.isEnum) { "type '$this' is enum" }
    require(!isAbstract) { "type '$this' is abstract" }
    return MetaClass(
        this,
        memberProperties
            .filter { !isSubclassOf(Throwable::class) || (it.name != "cause" && it.name != "message") }
            .sortedBy { it.name }
            .map { property ->
                @Suppress("UNCHECKED_CAST") (property.returnType.classifier as KClass<*>).metaProperty(
                    property as KProperty1<Any, Any?>, baseEncoderTypes, property.returnType.isMarkedNullable
                )
            },
        (primaryConstructor ?: error("'$this' has no primary constructor")).valueParameters.map { it.name!! }
    )
}

public fun reflectionBinarySerializer(
    baseEncoders: List<BaseEncoder<*>>,
    treeConcreteClasses: List<KClass<*>> = emptyList(),
    graphConcreteClasses: List<KClass<*>> = emptyList(),
): BinarySerializer {
    val baseEncoderTypes = baseEncoders.map { it.type }
    val encoders = mutableListOf<Encoder>()
    encoders.addAll(baseEncoders)
    fun List<KClass<*>>.add(graph: Boolean) = forEach { klass ->
        val metaClass = klass.metaClass(baseEncoderTypes)
        @Suppress("UNCHECKED_CAST") encoders.add(ClassEncoder(klass as KClass<Any>, graph,
            { writer, instance ->
                metaClass.properties.forEach { it.write(writer, it.property.get(instance)) }
            },
            { reader ->
                val parameterProperties = Array(metaClass.parameterProperties.size) { metaClass.parameterProperties[it].read(reader) }
                val created = klass.primaryConstructor!!.call(*parameterProperties)
                val instance = if (graph) reader.created(created) else created
                metaClass.bodyProperties.forEach { it.mutableProperty().set(instance, it.read(reader)) }
                instance
            }
        ))
    }
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)
    return BinarySerializer(encoders)
}

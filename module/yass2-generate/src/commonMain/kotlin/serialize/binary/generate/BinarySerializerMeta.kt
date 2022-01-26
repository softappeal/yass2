package ch.softappeal.yass2.serialize.binary.generate

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*

internal enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

internal class MetaProperty(
    val property: KProperty1<Any, Any?>,
    val kind: PropertyKind,
    val encoderId: Int = -1,
) {
    fun mutableProperty(): KMutableProperty1<Any, Any?> = property as KMutableProperty1<Any, Any?>
}

internal fun KClass<*>.metaProperty(
    property: KProperty1<Any, Any?>,
    baseEncoderTypes: List<KClass<*>>,
    concreteClasses: List<KClass<*>>,
    optional: Boolean,
    isStrictSubclassOf: KClass<*>.(base: KClass<*>) -> Boolean,
): MetaProperty {
    val kind = if (optional) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
    return if (this == List::class) {
        MetaProperty(property, kind, ListEncoderId.id)
    } else {
        val baseEncoderIndex = baseEncoderTypes.indexOfFirst { it == this }
        if (baseEncoderIndex >= 0) {
            MetaProperty(property, kind, baseEncoderIndex + FirstEncoderId)
        } else {
            val concreteClassIndex = concreteClasses.indexOfFirst { it == this }
            if (concreteClassIndex >= 0 && concreteClasses.none { it.isStrictSubclassOf(concreteClasses[concreteClassIndex]) }) {
                MetaProperty(property, kind, concreteClassIndex + baseEncoderTypes.size + FirstEncoderId)
            } else {
                MetaProperty(property, PropertyKind.WithId)
            }
        }
    }
}

internal class MetaClass(
    klass: KClass<*>,
    properties: List<MetaProperty>,
    parameterNames: List<String>,
) {
    val parameterProperties: List<MetaProperty>
    val bodyProperties: List<MetaProperty>
    val properties: List<MetaProperty>

    init {
        parameterProperties = mutableListOf()
        bodyProperties = mutableListOf()
        val propertyNames = properties.map { it.property.name }
        parameterNames.forEach { parameterName ->
            require(propertyNames.indexOf(parameterName) >= 0) {
                "primary constructor parameter '$parameterName' of '$klass' is not a property"
            }
            parameterProperties.add(properties.first { it.property.name == parameterName })
        }
        properties.forEach { property ->
            if (property.property.name !in parameterNames) {
                try {
                    property.mutableProperty()
                } catch (e: Exception) {
                    throw IllegalArgumentException("body property '${property.property.name}' of '$klass' is not 'var'")
                }
                bodyProperties.add(property)
            }
        }
        this.properties = parameterProperties + bodyProperties
    }
}

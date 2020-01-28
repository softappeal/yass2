package ch.softappeal.yass2.serialize.binary

import kotlin.reflect.*

internal enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

internal class MetaProperty(val property: KProperty1<Any, Any?>, val kind: PropertyKind, val encoderId: Int = -1) {
    fun mutableProperty() = property as KMutableProperty1<Any, Any?>

    fun write(writer: EncoderWriter, value: Any?): Unit = when (kind) {
        PropertyKind.WithId -> writer.writeWithId(value)
        PropertyKind.NoIdRequired -> writer.writeNoIdRequired(encoderId, value!!)
        PropertyKind.NoIdOptional -> writer.writeNoIdOptional(encoderId, value)
    }

    fun read(reader: EncoderReader): Any? = when (kind) {
        PropertyKind.WithId -> reader.readWithId()
        PropertyKind.NoIdRequired -> reader.readNoIdRequired(encoderId)
        PropertyKind.NoIdOptional -> reader.readNoIdOptional(encoderId)
    }
}

internal fun KClass<*>.metaProperty(
    property: KProperty1<Any, Any?>,
    baseEncoderTypes: List<KClass<*>>, concreteClasses: List<KClass<*>>,
    optional: Boolean, isStrictSubclassOf: KClass<*>.(base: KClass<*>) -> Boolean
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

internal class MetaClass(klass: KClass<*>, val properties: List<MetaProperty>, val parameterNames: List<String>) {
    val parameterIndices: List<Int>
    val varIndices: List<Int>

    init {
        parameterIndices = mutableListOf()
        varIndices = mutableListOf()
        val propertyNames = properties.map { it.property.name }
        parameterNames.forEach { parameter ->
            val propertyIndex = propertyNames.indexOf(parameter)
            require(propertyIndex >= 0) { "primary constructor parameter '$parameter' of '$klass' is not a property" }
            parameterIndices.add(propertyIndex)
        }
        properties.withIndex().forEach { (index, property) ->
            if (property.property.name !in parameterNames) {
                try {
                    property.mutableProperty()
                } catch (e: Exception) {
                    throw IllegalArgumentException("body property '${property.property.name}' of '$klass' is not 'var'")
                }
                varIndices.add(index)
            }
        }
    }
}

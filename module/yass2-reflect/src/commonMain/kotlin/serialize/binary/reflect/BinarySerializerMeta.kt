package ch.softappeal.yass2.serialize.binary.reflect

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*

public enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

public class MetaProperty internal constructor(
    public val property: KProperty1<Any, Any?>,
    public val kind: PropertyKind,
    public val encoderId: Int = -1,
) {
    internal fun mutableProperty(): KMutableProperty1<Any, Any?> = property as KMutableProperty1<Any, Any?>

    internal fun write(writer: EncoderWriter, value: Any?): Unit = when (kind) {
        PropertyKind.WithId -> writer.writeWithId(value)
        PropertyKind.NoIdRequired -> writer.writeNoIdRequired(encoderId, value!!)
        PropertyKind.NoIdOptional -> writer.writeNoIdOptional(encoderId, value)
    }

    internal fun read(reader: EncoderReader): Any? = when (kind) {
        PropertyKind.WithId -> reader.readWithId()
        PropertyKind.NoIdRequired -> reader.readNoIdRequired(encoderId)
        PropertyKind.NoIdOptional -> reader.readNoIdOptional(encoderId)
    }
}

internal fun KClass<*>.metaProperty(
    property: KProperty1<Any, Any?>,
    baseEncoderTypes: List<KClass<*>>,
    optional: Boolean,
): MetaProperty {
    val kind = if (optional) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
    return if (this == List::class) {
        MetaProperty(property, kind, ListEncoderId.id)
    } else {
        val baseEncoderIndex = baseEncoderTypes.indexOfFirst { it == this }
        if (baseEncoderIndex >= 0) {
            MetaProperty(property, kind, baseEncoderIndex + FirstEncoderId)
        } else {
            MetaProperty(property, PropertyKind.WithId)
        }
    }
}

public class MetaClass internal constructor(
    klass: KClass<*>,
    properties: List<MetaProperty>,
    parameterNames: List<String>,
) {
    public val parameterProperties: List<MetaProperty>
    public val bodyProperties: List<MetaProperty>
    public val properties: List<MetaProperty>

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

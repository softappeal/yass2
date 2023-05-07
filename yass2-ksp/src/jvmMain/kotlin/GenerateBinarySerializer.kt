package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

private class MetaProperty(
    val property: KProperty1<Any, Any?>,
    val kind: PropertyKind,
    val encoderId: Int = -1,
) {
    fun mutableProperty(): KMutableProperty1<Any, Any?> = property as KMutableProperty1<Any, Any?>
}

private fun KClass<*>.metaProperty(
    property: KProperty1<Any, Any?>,
    baseEncoderTypes: List<KClass<*>>,
    optional: Boolean,
): MetaProperty {
    val kind = if (optional) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
    return if (this == List::class) MetaProperty(property, kind, ListEncoderId.id) else {
        val index = baseEncoderTypes.indexOfFirst { it == this }
        if (index >= 0) MetaProperty(property, kind, index + FIRST_ENCODER_ID) else MetaProperty(property, PropertyKind.WithId)
    }
}

private class MetaClass(
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

private fun KClass<*>.metaClass(baseEncoderTypes: List<KClass<*>>): MetaClass {
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
                    property.returnType.isMarkedNullable,
                )
            },
        (primaryConstructor ?: error("'$this' has no primary constructor")).valueParameters.map { it.name!! }
    )
}

public fun Appendable.generateBinarySerializer(
    baseEncodersProperty: KProperty0<List<BaseEncoder<out Any>>>,
    treeConcreteClasses: List<KClass<*>> = emptyList(),
    graphConcreteClasses: List<KClass<*>> = emptyList(),
) {
    val baseEncoders = baseEncodersProperty.get()
    require(
        (baseEncoders.map { it.type }.toSet() + treeConcreteClasses.toSet() + graphConcreteClasses.toSet()).size ==
            (baseEncoders.size + treeConcreteClasses.size + graphConcreteClasses.size)
    ) { "duplicated types" }
    write("""
        @Suppress("RedundantSuppression", "UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public val GeneratedBinarySerializer: ${BinarySerializer::class.qualifiedName} =
            ${BinarySerializer::class.qualifiedName}(${baseEncodersProperty.javaField!!.declaringClass.packageName}.${baseEncodersProperty.name} + listOf(
    """)
    val baseEncoderTypes = baseEncoders.map { it.type }
    fun List<KClass<*>>.add(graph: Boolean) = forEach { klass ->
        write("""
            ${ClassEncoder::class.qualifiedName}(${klass.qualifiedName}::class, $graph,
        """, 2)
        val metaClass = klass.metaClass(baseEncoderTypes)
        fun MetaProperty.encoderId(tail: String = ""): String = if (kind != PropertyKind.WithId) encoderId.toString() + tail else ""
        if (metaClass.properties.isEmpty()) {
            write("{ _, _ -> },", 3)
        } else {
            write("""
                { w, i ->
            """, 3)
            metaClass.properties.forEach { write("w.write${it.kind}(${it.encoderId(", ")}i.${it.property.name})", 4) }
            write("""
                },
            """, 3)
        }
        write("""
            {${if (graph || metaClass.properties.isNotEmpty()) " r ->" else ""}
        """, 3)
        write("""
            val i = ${if (graph) "r.created(" else ""}${klass.qualifiedName}(
        """, 4)
        fun cast(p: MetaProperty) = if (p.property.returnType.needsCast()) " as ${p.property.returnType}" else ""
        metaClass.parameterProperties.forEach { write("r.read${it.kind}(${it.encoderId()})${cast(it)},", 5) }
        write("""
            )${if (graph) ')' else ""}
        """, 4)
        metaClass.bodyProperties.forEach {
            write("i.${it.property.name} = r.read${it.kind}(${it.encoderId()})${cast(it)}", 4)
        }
        write("""
                    i
                }
            ),
        """, 2)
    }
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)
    write("""
        ))
    """, 1)
}

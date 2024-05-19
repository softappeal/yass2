package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.PropertyKind
import ch.softappeal.yass2.generate.appendLine
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.ClassEncoder
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.ListEncoderId
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

public fun Appendable.generateBinarySerializer(
    baseEncoderClasses: List<KClass<*>>,
    enumClasses: List<KClass<*>>,
    treeConcreteClasses: List<KClass<*>>,
    graphConcreteClasses: List<KClass<*>>,
) {
    val baseEncoderTypes = baseEncoderClasses.getBaseEncoderTypes() + enumClasses

    class Property(val property: KProperty1<out Any, *>) {
        var kind: PropertyKind
        var encoderId: Int = -1

        init {
            val type = property.returnType.classifier as KClass<*>
            kind = if (property.returnType.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
            if (type == List::class) {
                encoderId = ListEncoderId.id
            } else {
                val index = baseEncoderTypes.indexOfFirst { it == type }
                if (index >= 0) encoderId = index + FIRST_ENCODER_ID else kind = PropertyKind.WithId
            }
        }
    }

    class Properties(klass: KClass<*>) {
        val parameter: List<Property>
        val body: List<Property>
        val all: List<Property>

        init {
            require(!klass.isAbstract) { "class '${klass.qualifiedName}' must be concrete" }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
            parameter = buildList {
                val primaryConstructor = klass.primaryConstructor ?: error(
                    "class '${klass.qualifiedName}' must hava a primary constructor"
                )
                val propertyNames = properties.map { it.property.name }
                val parameterNames = primaryConstructor.valueParameters.map { it.name }
                parameterNames.forEach { parameterName ->
                    require(propertyNames.contains(parameterName)) {
                        "primary constructor parameter '$parameterName' of class '${klass.qualifiedName}' must be a property"
                    }
                    add(properties.first { it.property.name == parameterName })
                }
            }
            body = buildList {
                properties
                    .filter { it !in parameter }
                    .forEach { property ->
                        require(property.property is KMutableProperty1<out Any, *>) {
                            "body property '${property.property.name}' of '${klass.qualifiedName}' must be 'var'"
                        }
                        add(property)
                    }
            }
            all = parameter + body
        }
    }

    enumClasses.forEachIndexed { enumClassIndex, enumClass ->
        if (enumClassIndex == 0) appendLine()
        appendLine("private class EnumEncoder${enumClassIndex + 1} : ${EnumEncoder::class.qualifiedName}<${enumClass.qualifiedName}>(")
        appendLine(1, "${enumClass.qualifiedName}::class, kotlin.enumValues()")
        appendLine(")")
    }
    appendLine()
    appendLine("public fun createSerializer(): ${BinarySerializer::class.qualifiedName} =")
    appendLine(1, "${BinarySerializer::class.qualifiedName}(listOf(")
    baseEncoderClasses.forEach { type -> appendLine(2, "${type.qualifiedName}(),") }
    for (enumEncoderIndex in 1..enumClasses.size) appendLine(2, "EnumEncoder$enumEncoderIndex(),")

    fun List<KClass<*>>.add(graph: Boolean) = forEach { type ->
        fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
        appendLine(2, "${ClassEncoder::class.qualifiedName}(${type.qualifiedName}::class, $graph,")
        val properties = Properties(type)
        if (properties.all.isEmpty()) {
            appendLine(3, "{ _, _ -> },")
        } else {
            appendLine(3, "{ w, i ->")
            properties.all.forEach { property ->
                appendLine(4, "w.write${property.kind}(${property.encoderId(", ")}i.${property.property.name})")
            }
            appendLine(3, "},")
        }
        appendLine(3, "{${if (graph || properties.all.isNotEmpty()) " r ->" else ""}")
        appendLine(4, "val i = ${if (graph) "r.created(" else ""}${type.qualifiedName}(")
        properties.parameter.forEach { property ->
            appendLine(5, "r.read${property.kind}(${property.encoderId()}) as ${property.property.returnType},")
        }
        appendLine(4, ")${if (graph) ")" else ""}")
        properties.body.forEach { property ->
            appendLine(
                4,
                "i.${property.property.name} = r.read${property.kind}(${property.encoderId()}) as ${property.property.returnType}"
            )
        }
        appendLine(4, "i")
        appendLine(3, "}")
        appendLine(2, "),")
    }
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)

    appendLine(1, "))")
}

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.serialize.binary.BaseEncoder
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.ClassEncoder
import ch.softappeal.yass2.serialize.binary.FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.ListEncoderId
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

public fun CodeWriter.generateBinarySerializer(
    baseEncoders: List<BaseEncoder<out Any>>,
    treeConcreteClasses: List<KClass<*>>,
    graphConcreteClasses: List<KClass<*>> = emptyList(),
) {
    val baseEncoderTypes = baseEncoders.map { it.type }

    require(
        (baseEncoderTypes + treeConcreteClasses + graphConcreteClasses).toSet().size ==
            (baseEncoders.size + treeConcreteClasses.size + graphConcreteClasses.size)
    ) { "class must not be duplicated" }
    checkNotEnum(treeConcreteClasses + graphConcreteClasses, "belongs to 'baseEncoders'")

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

    writeLine()
    writeNestedLine("public fun createSerializer(") {
        writeNestedLine("baseEncoders: kotlin.collections.List<${BaseEncoder::class.qualifiedName}<out kotlin.Any>>,")
    }
    writeNestedLine("): ${BinarySerializer::class.qualifiedName} =") {
        writeNestedLine("${BinarySerializer::class.qualifiedName}(baseEncoders + listOf(") {

            fun List<KClass<*>>.add(graph: Boolean) = forEach { type ->
                fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
                writeNestedLine("${ClassEncoder::class.qualifiedName}(${type.qualifiedName}::class, $graph,") {
                    val properties = Properties(type)
                    if (properties.all.isEmpty()) {
                        writeNestedLine("{ _, _ -> },")
                    } else {
                        writeNestedLine("{ w, i ->") {
                            properties.all.forEach { property ->
                                writeNestedLine("w.write${property.kind}(${property.encoderId(", ")}i.${property.property.name})")
                            }
                        }
                        writeNestedLine("},")
                    }
                    writeNestedLine("{${if (graph || properties.all.isNotEmpty()) " r ->" else ""}") {
                        writeNestedLine("val i = ${if (graph) "r.created(" else ""}${type.qualifiedName}(") {
                            properties.parameter.forEach { property ->
                                writeNestedLine("r.read${property.kind}(${property.encoderId()}) as ${property.property.returnType},")
                            }
                        }
                        writeNestedLine(")${if (graph) ")" else ""}")
                        properties.body.forEach { property ->
                            writeNestedLine("i.${property.property.name} = r.read${property.kind}(${property.encoderId()}) as ${property.property.returnType}")
                        }
                        writeNestedLine("i")
                    }
                    writeNestedLine("}")
                }
                writeNestedLine("),")
            }

            treeConcreteClasses.add(false)
            graphConcreteClasses.add(true)
        }
        writeNestedLine("))")
    }
}

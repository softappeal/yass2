package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.PropertyKind
import ch.softappeal.yass2.serialize.binary.BaseEncoder
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

private fun List<KClass<out BaseEncoder<*>>>.getBaseEncoderTypes() =
    map { it.supertypes.first().arguments.first().type!!.classifier as KClass<*> }

public fun CodeWriter.generateBinarySerializer(
    baseEncoderClasses: List<KClass<out BaseEncoder<*>>>,
    enumClasses: List<KClass<*>>,
    treeConcreteClasses: List<KClass<*>>,
    graphConcreteClasses: List<KClass<*>> = emptyList(),
) {
    val baseTypes = baseEncoderClasses.getBaseEncoderTypes()
    val baseClasses = baseTypes + enumClasses

    (baseClasses + treeConcreteClasses + graphConcreteClasses).checkNotDuplicated()
    checkNotEnum(baseTypes + treeConcreteClasses + graphConcreteClasses, "belongs to enumClasses")
    enumClasses.forEach {
        require(it.isEnum()) { "class ${it.qualifiedName} in enumClasses must be enum" }
    }

    class Property(val property: KProperty1<out Any, *>) {
        var kind: PropertyKind
        var encoderId: Int = -1

        init {
            val type = property.returnType.classifier as KClass<*>
            kind = if (property.returnType.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
            if (type == List::class) {
                encoderId = ListEncoderId.id
            } else {
                val index = baseClasses.indexOfFirst { it == type }
                if (index >= 0) encoderId = index + FIRST_ENCODER_ID else kind = PropertyKind.WithId
            }
        }
    }

    class Properties(klass: KClass<*>) {
        val parameter: List<Property>
        val body: List<Property>
        val all: List<Property>

        init {
            require(!klass.isAbstract) { "class ${klass.qualifiedName} must be concrete" }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
            parameter = buildList {
                val primaryConstructor = klass.primaryConstructor ?: error(
                    "class ${klass.qualifiedName} must hava a primary constructor"
                )
                val parameterNames = primaryConstructor.valueParameters.map { it.name }
                val propertyNames = properties.map { it.property.name }
                parameterNames.forEach { parameterName ->
                    require(propertyNames.contains(parameterName)) {
                        "primary constructor parameter $parameterName of class ${klass.qualifiedName} must be a property"
                    }
                    add(properties.first { it.property.name == parameterName })
                }
            }
            body = buildList {
                properties
                    .filter { it !in parameter }
                    .forEach { property ->
                        require(property.property is KMutableProperty1<out Any, *>) {
                            "body property ${property.property.name} of ${klass.qualifiedName} must be var"
                        }
                        add(property)
                    }
            }
            all = parameter + body
        }
    }

    enumClasses.forEachIndexed { enumClassIndex, enumClass ->
        writeLine()
        writeNestedLine("private class EnumEncoder${enumClassIndex + 1} : ${EnumEncoder::class.qualifiedName}<${enumClass.qualifiedName}>(") {
            writeNestedLine("${enumClass.qualifiedName}::class, kotlin.enumValues()")
        }
        writeNestedLine(")")
    }

    writeLine()
    writeNestedLine("public fun createBinarySerializer(): ${BinarySerializer::class.qualifiedName} =") {
        writeNestedLine("${BinarySerializer::class.qualifiedName}(listOf(") {
            baseEncoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
            for (enumEncoderIndex in 1..enumClasses.size) writeNestedLine("EnumEncoder$enumEncoderIndex(),")

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

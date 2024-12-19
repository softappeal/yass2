package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.PropertyKind
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.ClassEncoder
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.ListEncoderId
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

private fun List<KSType>.getBaseEncoderTypes() =
    map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }

internal fun CodeWriter.generateBinarySerializer(
    baseEncoderClasses: List<KSType>,
    enumClasses: List<KSType>,
    treeConcreteClasses: List<KSType>,
    graphConcreteClasses: List<KSType>,
    declaration: KSPropertyDeclaration,
) {
    val baseTypes = baseEncoderClasses.getBaseEncoderTypes()
    val baseClasses = baseTypes + enumClasses

    (baseClasses + treeConcreteClasses + graphConcreteClasses).checkNotDuplicated(declaration)
    checkNotEnum(declaration, baseTypes + treeConcreteClasses + graphConcreteClasses, "belongs to enumClasses")
    enumClasses.forEach {
        require(it.isEnum()) { "class ${it.qualifiedName} in enumClasses must be enum @${declaration.location}" }
    }

    class Property(val property: KSPropertyDeclaration) {
        var kind: PropertyKind
        var encoderId: Int = -1

        init {
            val type = property.type.resolve()
            kind = if (type.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
            val typeNotNullable = type.makeNotNullable()
            val typeNotNullableName = typeNotNullable.qualifiedName
            if (typeNotNullableName == List::class.qualifiedName || typeNotNullableName == "kotlin.collections.MutableList") {
                encoderId = ListEncoderId.id
            } else {
                val index = baseClasses.indexOfFirst { it == typeNotNullable }
                if (index >= 0) encoderId = index + FIRST_ENCODER_ID else kind = PropertyKind.WithId
            }
        }
    }

    class Properties(klass: KSClassDeclaration) {
        val parameter: List<Property>
        val body: List<Property>
        val all: List<Property>

        init {
            require(!klass.isAbstract()) { "class ${klass.qualifiedName()} must be concrete @${declaration.location}" }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
            parameter = buildList {
                val primaryConstructor = klass.primaryConstructor ?: error(
                    "class ${klass.qualifiedName()} must hava a primary constructor @${declaration.location}"
                )
                val parameters = primaryConstructor.parameters
                parameters.forEach { parameter ->
                    require(parameter.isVal || parameter.isVar) {
                        "primary constructor parameter ${parameter.name!!.asString()} of class ${klass.qualifiedName()} must be a property @${declaration.location}"
                    }
                    add(properties.first { it.property.name == parameter.name!!.asString() })
                }
            }
            body = buildList {
                properties
                    .filter { it !in parameter }
                    .forEach { property ->
                        require(property.property.isMutable) {
                            "body property ${property.property.name} of ${property.property.parentDeclaration?.qualifiedName()} must be var @${declaration.location}"
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
    writeNestedLine("public ${declaration.actual()}fun createBinarySerializer(): ${BinarySerializer::class.qualifiedName} =") {
        writeNestedLine("${BinarySerializer::class.qualifiedName}(listOf(") {
            baseEncoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
            for (enumEncoderIndex in 1..enumClasses.size) writeNestedLine("EnumEncoder$enumEncoderIndex(),")

            fun List<KSType>.add(graph: Boolean) = forEach { type ->
                fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
                writeNestedLine("${ClassEncoder::class.qualifiedName}(${type.qualifiedName}::class, $graph,") {
                    val properties = Properties(type.declaration as KSClassDeclaration)
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
                                writeNestedLine("r.read${property.kind}(${property.encoderId()}) as ${property.property.type.type()},")
                            }
                        }
                        writeNestedLine(")${if (graph) ")" else ""}")
                        properties.body.forEach { property ->
                            writeNestedLine("i.${property.property.name} = r.read${property.kind}(${property.encoderId()}) as ${property.property.type.type()}")
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

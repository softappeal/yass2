package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.PropertyKind
import ch.softappeal.yass2.generate.append
import ch.softappeal.yass2.generate.appendLine
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.ClassEncoder
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.ListEncoderId
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

internal fun Appendable.generateBinarySerializer(
    baseEncoderClasses: List<KSType>,
    enumClasses: List<KSType>,
    treeConcreteClasses: List<KSType>,
    graphConcreteClasses: List<KSType>,
) {
    val baseEncoderTypes = baseEncoderClasses.getBaseEncoderTypes() + enumClasses

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
                val index = baseEncoderTypes.indexOfFirst { it == typeNotNullable }
                if (index >= 0) encoderId = index + FIRST_ENCODER_ID else kind = PropertyKind.WithId
            }
        }
    }

    class Properties(klass: KSClassDeclaration) {
        val parameter: List<Property>
        val body: List<Property>
        val all: List<Property>

        init {
            require(!klass.isAbstract()) { "class '${klass.qualifiedName()}' must be concrete @${klass.location}" }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
            parameter = buildList {
                val primaryConstructor = klass.primaryConstructor ?: error(
                    "class '${klass.qualifiedName()}' must hava a primary constructor @${klass.location}"
                )
                val parameters = primaryConstructor.parameters
                parameters
                    .firstOrNull { !it.isVal && !it.isVar }
                    ?.let { parameter ->
                        error("primary constructor parameter '${parameter.name!!.asString()}' of class '${klass.qualifiedName()}' must be a property @${parameter.location}")
                    }
                parameters.forEach { parameter ->
                    add(properties.first { it.property.name == parameter.name!!.asString() })
                }
            }
            body = buildList {
                properties
                    .filter { it !in parameter }
                    .forEach { property ->
                        require(property.property.isMutable) {
                            "body property '${property.property.name}' of '${property.property.parentDeclaration?.qualifiedName()}' must be 'var' @${property.property.location}"
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

    fun List<KSType>.add(graph: Boolean) = forEach { type ->
        fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
        appendLine(2, "${ClassEncoder::class.qualifiedName}(${type.qualifiedName}::class, $graph,")
        val properties = Properties(type.declaration as KSClassDeclaration)
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
            append(5, "r.read${property.kind}(${property.encoderId()}) as ").appendType(property.property.type).appendLine(',')
        }
        appendLine(4, ")${if (graph) ")" else ""}")
        properties.body.forEach { property ->
            append(4, "i.${property.property.name} = r.read${property.kind}(${property.encoderId()}) as ")
                .appendType(property.property.type).appendLine()
        }
        appendLine(4, "i")
        appendLine(3, "}")
        appendLine(2, "),")
    }
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)

    appendLine(1, "))")
}

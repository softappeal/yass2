package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.ClassEncoder
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.ListEncoderId
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

internal fun Appendable.generateBinarySerializer(
    baseEncoderClasses: List<KSType>,
    treeConcreteClasses: List<KSType>,
    graphConcreteClasses: List<KSType>,
    enumClasses: List<KSType>,
) {
    val baseEncoderTypes = baseEncoderClasses.getBaseEncoderTypes() + enumClasses

    class Property(val declaration: KSPropertyDeclaration) {
        var kind: PropertyKind
            private set
        var encoderId: Int = -1
            private set

        init {
            val type = declaration.type.resolve()
            kind = if (type.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
            val typeNotNullable = type.makeNotNullable()
            val typeNotNullableName = typeNotNullable.qualifiedName()
            if (typeNotNullableName == List::class.qualifiedName || typeNotNullableName == "kotlin.collections.MutableList") {
                encoderId = ListEncoderId.id
            } else {
                val baseEncoderIndex = baseEncoderTypes.indexOfFirst { it == typeNotNullable }
                if (baseEncoderIndex >= 0) encoderId = baseEncoderIndex + FIRST_ENCODER_ID else kind = PropertyKind.WithId
            }
        }
    }

    class Properties(klass: KSClassDeclaration) {
        val parameter: List<Property>
        val body: List<Property>
        val all: List<Property>

        init {
            require(klass.classKind == ClassKind.CLASS) { "'${klass.qualifiedName()}' must be a regular class @${klass.location}" }
            require(!klass.isAbstract()) { "class '${klass.qualifiedName()}' must not be abstract @${klass.location}" }
            val primaryConstructor = klass.primaryConstructor ?: error(
                "class '${klass.qualifiedName()}' must hava a primary constructor @${klass.location}"
            )
            val valueParameters = primaryConstructor.parameters
            valueParameters
                .firstOrNull { !it.isVal && !it.isVar }
                ?.let { parameter ->
                    error(
                        "primary constructor parameter '${parameter.name!!.asString()}' of class " +
                            "'${klass.qualifiedName()}' must be a property @${parameter.location}"
                    )
                }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
            parameter = buildList {
                valueParameters.forEach { valueParameter ->
                    add(properties.first { it.declaration.simpleName() == valueParameter.name!!.asString() })
                }
            }
            body = buildList {
                properties
                    .filter { it !in parameter }
                    .forEach { property ->
                        require(property.declaration.isMutable) {
                            "body property '${property.declaration.simpleName()}' of " +
                                "'${property.declaration.parentDeclaration?.qualifiedName()}' must be 'var' @${property.declaration.location}"
                        }
                        add(property)
                    }
            }
            all = parameter + body
        }
    }

    enumClasses.forEachIndexed { enumClassIndex, enumClass ->
        if (enumClassIndex == 0) appendLine()
        appendLine("private class EnumEncoder${enumClassIndex + 1} : ${EnumEncoder::class.qualifiedName}<${enumClass.qualifiedName()}>(")
        appendLine(1, "${enumClass.qualifiedName()}::class, kotlin.enumValues()")
        appendLine(")")
    }
    appendLine()
    appendLine("public fun createSerializer(): ${BinarySerializer::class.qualifiedName} =")
    appendLine(1, "${BinarySerializer::class.qualifiedName}(listOf(")
    baseEncoderClasses.forEach { type ->
        appendLine(2, "${type.qualifiedName()}(),")
    }
    for (enumEncoderIndex in 1..enumClasses.size) appendLine(2, "EnumEncoder$enumEncoderIndex(),")

    fun List<KSType>.add(graph: Boolean) = forEach { type ->
        fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
        appendLine(2, "${ClassEncoder::class.qualifiedName}(${type.qualifiedName()}::class, $graph,")
        val properties = Properties(type.declaration as KSClassDeclaration)
        if (properties.all.isEmpty()) {
            appendLine(3, "{ _, _ -> },")
        } else {
            appendLine(3, "{ w, i ->")
            properties.all.forEach { property ->
                appendLine(4, "w.write${property.kind}(${property.encoderId(", ")}i.${property.declaration.simpleName()})")
            }
            appendLine(3, "},")
        }
        appendLine(3, "{${if (graph || properties.all.isNotEmpty()) " r ->" else ""}")
        appendLine(4, "val i = ${if (graph) "r.created(" else ""}${type.qualifiedName()}(")
        properties.parameter.forEach { property ->
            append(5, "r.read${property.kind}(${property.encoderId()}) as ").appendType(property.declaration.type).appendLine(',')
        }
        appendLine(4, ")${if (graph) ")" else ""}")
        properties.body.forEach { property ->
            append(4, "i.${property.declaration.simpleName()} = r.read${property.kind}(${property.encoderId()}) as ")
                .appendType(property.declaration.type).appendLine()
        }
        appendLine(4, "i")
        appendLine(3, "}")
        appendLine(2, "),")
    }
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)

    appendLine(1, "))")
}

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

internal fun Appendable.generateBinarySerializer(baseEncoderClasses: List<KSType>, treeConcreteClasses: List<KSType>, graphConcreteClasses: List<KSType>, enumClasses: List<KSType>) {
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
            val valueParameters = (klass.primaryConstructor ?: error("class '${klass.qualifiedName()}' must hava a primary constructor @${klass.location}")).parameters
            valueParameters.firstOrNull { !it.isVal && !it.isVar }?.let { parameter -> error("primary constructor parameter '${parameter.name!!.asString()}' of class '${klass.qualifiedName()}' must be a property @${parameter.location}") }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
            parameter = buildList {
                valueParameters.forEach { valueParameter ->
                    add(properties.first { it.declaration.simpleName() == valueParameter.name!!.asString() })
                }
            }
            body = buildList {
                properties.filter { it !in parameter }.forEach { property ->
                    require(property.declaration.isMutable) { "body property '${property.declaration.simpleName()}' of '${property.declaration.parentDeclaration?.qualifiedName()}' must be 'var' @${property.declaration.location}" }
                    add(property)
                }
            }
            all = parameter + body
        }
    }

    fun List<KSType>.add(graph: Boolean) = forEach { type ->
        fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
        appendLine(1, "${ClassEncoder::class.qualifiedName}(${type.qualifiedName()}::class, $graph,")
        val properties = Properties(type.declaration as KSClassDeclaration)
        if (properties.all.isEmpty()) {
            appendLine(2, "{ _, _ -> },")
        } else {
            appendLine(2, "{ w, i ->")
            properties.all.forEach { property -> appendLine(3, "w.write${property.kind}(${property.encoderId(", ")}i.${property.declaration.simpleName()})") }
            appendLine(2, "},")
        }
        append(2, "{").appendLine(if (graph || properties.all.isNotEmpty()) " r ->" else "")
        appendLine(3, "val i = ${if (graph) "r.created(" else ""}${type.qualifiedName()}(")
        properties.parameter.forEach { property ->
            append(4, "r.read${property.kind}(${property.encoderId()}) as ").appendType(property.declaration.type).appendLine(',')
        }
        append(3, ")").appendLine(if (graph) ")" else "")
        properties.body.forEach { property ->
            append(3, "i.${property.declaration.simpleName()} = r.read${property.kind}(${property.encoderId()}) as ").appendType(property.declaration.type).appendLine()
        }
        appendLine(2, "    i")
        appendLine(2, "}")
        appendLine(1, "),")
    }

    enumClasses.forEachIndexed { enumClassIndex, enumClass ->
        if (enumClassIndex == 0) appendLine()
        appendLine("private class EnumEncoder${enumClassIndex + 1} : ${EnumEncoder::class.qualifiedName}<${enumClass.qualifiedName()}>(${enumClass.qualifiedName()}::class, kotlin.enumValues())")
    }

    appendLine()
    appendLine("public fun createSerializer(): ${BinarySerializer::class.qualifiedName} = ${BinarySerializer::class.qualifiedName}(listOf(")
    baseEncoderClasses.forEach { type -> appendLine(1, "${type.qualifiedName()}(),") }
    for (enumEncoderIndex in 1..enumClasses.size) appendLine(1, "EnumEncoder$enumEncoderIndex(),")
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)
    appendLine("))")
}

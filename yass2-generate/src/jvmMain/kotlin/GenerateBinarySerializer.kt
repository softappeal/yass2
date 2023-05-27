package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

internal fun Appendable.generateBinarySerializer(baseEncoderClasses: List<KSType>, treeConcreteClasses: List<KSType>, graphConcreteClasses: List<KSType>) {
    val baseEncoderTypes = baseEncoderClasses.getBaseEncoderTypes()

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
            require(klass.classKind == ClassKind.CLASS) { "'${klass.qualifiedName()}' must be a regular class @${klass.location}" } // TODO: allow enum classes (no need for hand written enum encoder)
            require(!klass.isAbstract()) { "class '${klass.qualifiedName()}' must not be abstract @${klass.location}" }
            parameter = mutableListOf()
            body = mutableListOf()
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
            val propertyNames = properties.map { it.declaration.simpleName() }
            val parameterNames = (klass.primaryConstructor ?: error("class '${klass.qualifiedName()}' must hava a primary constructor @${klass.location}")).parameters.map { it.name!!.asString() }
            parameterNames.forEach { parameterName ->
                require(propertyNames.indexOf(parameterName) >= 0) { "primary constructor parameter '$parameterName' of class '${klass.qualifiedName()}' must be a property @${klass.location}" }
                parameter.add(properties.first { it.declaration.simpleName() == parameterName })
            }
            properties.filter { it.declaration.simpleName() !in parameterNames }.forEach { property ->
                require(property.declaration.isMutable) { "body property '${property.declaration.simpleName()}' of class '${property.declaration.parentDeclaration!!.qualifiedName()}' must be 'var' @${klass.location}" }
                body.add(property)
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

    appendLine()
    appendLine("public fun createSerializer(): ${BinarySerializer::class.qualifiedName} = ${BinarySerializer::class.qualifiedName}(listOf(")
    baseEncoderClasses.forEach { type -> appendLine(1, "${type.qualifiedName()}(),") }
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)
    appendLine("))")
}

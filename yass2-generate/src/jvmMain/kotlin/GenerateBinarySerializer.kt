package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

private class MetaProperty(val property: KSPropertyDeclaration, val kind: PropertyKind, val encoderId: Int = -1)

internal fun Appendable.generateBinarySerializer(baseEncoderClasses: List<KSType>, treeConcreteClasses: List<KSType>, graphConcreteClasses: List<KSType>) {
    val baseEncoderTypes = baseEncoderClasses.map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }

    fun KSPropertyDeclaration.metaProperty(): MetaProperty {
        val type = type.resolve()
        val kind = if (type.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
        val notNullable = type.makeNotNullable()
        val name = notNullable.declaration.name()
        return if ((name == List::class.qualifiedName) || (name == "kotlin.collections.MutableList")) MetaProperty(this, kind, ListEncoderId.id) else {
            val index = baseEncoderTypes.indexOfFirst { it == notNullable }
            if (index >= 0) MetaProperty(this, kind, index + FIRST_ENCODER_ID) else MetaProperty(this, PropertyKind.WithId)
        }
    }

    class MetaClass(properties: List<MetaProperty>, parameterNames: List<String>) {
        val parameterProperties: List<MetaProperty>
        val bodyProperties: List<MetaProperty>
        val properties: List<MetaProperty>

        init {
            parameterProperties = mutableListOf()
            bodyProperties = mutableListOf()
            val propertyNames = properties.map { it.property.simpleName.asString() }
            parameterNames.forEach { parameterName ->
                require(propertyNames.indexOf(parameterName) >= 0) { "primary constructor parameter '$parameterName' of 'TODO' is not a property" } // TODO
                parameterProperties.add(properties.first { it.property.simpleName.asString() == parameterName })
            }
            properties.forEach { property ->
                if (property.property.simpleName.asString() !in parameterNames) {
                    require(property.property.isMutable) { "body property '${property.property}' of '${property.property.parentDeclaration!!.name()}' is not 'var'" }
                    bodyProperties.add(property)
                }
            }
            this.properties = parameterProperties + bodyProperties
        }
    }

    fun KSType.metaClass(): MetaClass {
        val klass = declaration as KSClassDeclaration
        require(klass.classKind != ClassKind.ENUM_CLASS) { "type '${klass.name()}' is enum" }
        require(!klass.isAbstract()) { "type '${klass.name()}' is abstract" }
        return MetaClass(
            klass.getAllProperties()
                .filterNot { it.isPropertyOfThrowable() }
                .toList()
                .sortedBy { it.simpleName.asString() }
                .map { it.metaProperty() },
            (klass.primaryConstructor ?: error("'${klass.name()}' has no primary constructor")).parameters.map { it.name!!.asString() }
        )
    }

    fun KSType.name() = (declaration as KSClassDeclaration).name()

    fun appendCast(p: MetaProperty) {
        append(" as ")
        appendType(p.property.type)
    }

    fun MetaProperty.encoderId(tail: String = ""): String = if (kind != PropertyKind.WithId) encoderId.toString() + tail else ""

    fun List<KSType>.add(graph: Boolean) = forEach { klass ->
        write("""
            ${ClassEncoder::class.qualifiedName}(${klass.name()}::class, $graph,
        """, 1)
        val metaClass = klass.metaClass()
        if (metaClass.properties.isEmpty()) {
            write("{ _, _ -> },", 2)
        } else {
            write("""
                { w, i ->
            """, 2)
            metaClass.properties.forEach { write("w.write${it.kind}(${it.encoderId(", ")}i.${it.property.simpleName.asString()})", 3) }
            write("""
                },
            """, 2)
        }
        write("""
            {${if (graph || metaClass.properties.isNotEmpty()) " r ->" else ""}
        """, 2)
        write("""
            val i = ${if (graph) "r.created(" else ""}${klass.name()}(
        """, 3)
        metaClass.parameterProperties.forEach {
            append("                r.read${it.kind}(${it.encoderId()})")
            appendCast(it)
            appendLine(',')
        }
        write("""
            )${if (graph) ')' else ""}
        """, 3)
        metaClass.bodyProperties.forEach {
            append("            i.${it.property.simpleName.asString()} = r.read${it.kind}(${it.encoderId()})")
            appendCast(it)
            appendLine()
        }
        write("""
                    i
                }
            ),
        """, 1)
    }

    write("""

        public fun createSerializer(): ${BinarySerializer::class.qualifiedName} = ${BinarySerializer::class.qualifiedName}(listOf(
    """)
    baseEncoderClasses.forEach { appendLine("    ${it.name()}(),") }
    treeConcreteClasses.add(false)
    graphConcreteClasses.add(true)
    write("""
        ))
    """)
}

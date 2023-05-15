package ch.softappeal.yass2.generate.ksp // TODO

import ch.softappeal.yass2.serialize.binary.*
import com.google.devtools.ksp.symbol.*

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

private class MetaProperty(val property: KSPropertyDeclaration, val kind: PropertyKind, val encoderId: Int = -1)

internal fun Appendable.generateBinarySerializer(annotation: KSAnnotation) {
    @Suppress("UNCHECKED_CAST") val baseEncoderClasses = annotation.arguments[0].value as List<KSType>
    @Suppress("UNCHECKED_CAST") val treeConcreteClasses = annotation.arguments[1].value as List<KSType>
    @Suppress("UNCHECKED_CAST") val graphConcreteClasses = annotation.arguments[2].value as List<KSType>

    fun KSType.metaProperty(property: KSPropertyDeclaration, baseEncoderClasses: List<KSType>, optional: Boolean): MetaProperty {
        val kind = if (optional) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
        return if (this == List::class) MetaProperty(property, kind, ListEncoderId.id) else {
            val index = baseEncoderClasses.indexOfFirst { it == this }
            if (index >= 0) MetaProperty(property, kind, index + FIRST_ENCODER_ID) else MetaProperty(property, PropertyKind.WithId)
        }
    }

    class MetaClass(properties: Sequence<MetaProperty>, parameterNames: List<String>) {
        val parameterProperties: List<MetaProperty>
        val bodyProperties: List<MetaProperty>
        val properties: List<MetaProperty>

        init {
            parameterProperties = mutableListOf()
            bodyProperties = mutableListOf()
            parameterNames.forEach { parameterName ->
                parameterProperties.add(properties.first { it.property.simpleName.asString() == parameterName })
            }
            properties.forEach { property ->
                if (property.property.simpleName.asString() !in parameterNames) bodyProperties.add(property)
            }
            this.properties = parameterProperties + bodyProperties
        }
    }

    fun KSType.metaClass(baseEncoderClasses: List<KSType>): MetaClass {
        val klass = declaration as KSClassDeclaration
        return MetaClass(
            klass.getAllProperties()
                .filter { /* TODO !isSubclassOf(Throwable::class)  || */ (it.simpleName.asString() != "cause" && it.simpleName.asString() != "message") }
                .sortedBy { it.simpleName.asString() }
                .map { property ->
                    property.type.resolve().metaProperty(
                        property,
                        baseEncoderClasses,
                        property.type.resolve().isMarkedNullable,
                    )
                },
            (klass.primaryConstructor ?: error("'$this' has no primary constructor")).parameters.map { it.name!!.asString() }
        )
    }

    fun KSType.name() = (declaration as KSClassDeclaration).name()

    println(baseEncoderClasses)
    println(treeConcreteClasses)
    println(graphConcreteClasses)
    /*
    write("""

        public val GeneratedBinarySerializer: ${BinarySerializer::class.qualifiedName} =
            ${BinarySerializer::class.qualifiedName}(listOf(
    """)
    baseEncoderClasses.forEach { appendLine("        ${it.name()}(),") }
    fun List<KSType>.add(graph: Boolean) = forEach { klass ->
        write("""
            ${ClassEncoder::class.qualifiedName}(${klass.name()}::class, $graph,
        """, 2)
        val metaClass = klass.metaClass(baseEncoderClasses)
        fun MetaProperty.encoderId(tail: String = ""): String = if (kind != PropertyKind.WithId) encoderId.toString() + tail else ""
        if (metaClass.properties.isEmpty()) {
            write("{ _, _ -> },", 3)
        } else {
            write("""
                { w, i ->
            """, 3)
            metaClass.properties.forEach { write("w.write${it.kind}(${it.encoderId(", ")}i.${it.property.simpleName.asString()})", 4) }
            write("""
                },
            """, 3)
        }
        write("""
            {${if (graph || metaClass.properties.isNotEmpty()) " r ->" else ""}
        """, 3)
        write("""
            val i = ${if (graph) "r.created(" else ""}${klass.name()}(
        """, 4)
        fun cast(p: MetaProperty) = " as ${p.property.type.resolve().name()}"
        metaClass.parameterProperties.forEach { write("r.read${it.kind}(${it.encoderId()})${cast(it)},", 5) }
        write("""
            )${if (graph) ')' else ""}
        """, 4)
        metaClass.bodyProperties.forEach {
            write("i.${it.property.simpleName.asString()} = r.read${it.kind}(${it.encoderId()})${cast(it)}", 4)
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
     */
}

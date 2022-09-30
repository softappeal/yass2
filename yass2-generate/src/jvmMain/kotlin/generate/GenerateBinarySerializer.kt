package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.serialize.binary.reflect.*
import kotlin.reflect.*

public fun generateBinarySerializer(
    baseEncoders: List<BaseEncoder<*>>,
    treeConcreteClasses: List<KClass<*>> = emptyList(),
    graphConcreteClasses: List<KClass<*>> = emptyList(),
    name: String = "generatedBinarySerializer",
): String = writer {
    require(
        (baseEncoders.map { it.type }.toSet() + treeConcreteClasses.toSet() + graphConcreteClasses.toSet()).size ==
            (baseEncoders.size + treeConcreteClasses.size + graphConcreteClasses.size)
    ) { "duplicated types" }
    write("""
        @Suppress("RedundantSuppression", "UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public fun $name(
            baseEncoders: List<${BaseEncoder::class.qualifiedName}<*>>,
        ): ${BinarySerializer::class.qualifiedName} =
            ${BinarySerializer::class.qualifiedName}(baseEncoders + listOf(
    """)
    val baseEncoderTypes = baseEncoders.map { it.type }
    fun List<KClass<*>>.add(graph: Boolean) = forEach { klass ->
        write("""
            ${ClassEncoder::class.qualifiedName}(${klass.qualifiedName}::class, $graph,
        """, 2)
        val metaClass = klass.metaClass(baseEncoderTypes)
        fun MetaProperty.encoderId(tail: String = ""): String = if (kind != PropertyKind.WithId) encoderId.toString() + tail else ""
        if (metaClass.properties.isEmpty()) {
            write("{ _, _ -> },", 3)
        } else {
            write("""
                { w, i ->
            """, 3)
            metaClass.properties.forEach { write("w.write${it.kind}(${it.encoderId(", ")}i.${it.property.name})", 4) }
            write("""
                },
            """, 3)
        }
        write("""
            {${if (graph || metaClass.properties.isNotEmpty()) " r ->" else ""}
        """, 3)
        write("""
            val i = ${if (graph) "r.created(" else ""}${klass.qualifiedName}(
        """, 4)
        fun cast(p: MetaProperty) = if (p.property.returnType.needsCast()) " as ${p.property.returnType}" else ""
        metaClass.parameterProperties.forEach { write("r.read${it.kind}(${it.encoderId()})${cast(it)},", 5) }
        write("""
            )${if (graph) ')' else ""}
        """, 4)
        metaClass.bodyProperties.forEach {
            write("i.${it.property.name} = r.read${it.kind}(${it.encoderId()})${cast(it)}", 4)
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
}

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*

public fun generateBinarySerializer(
    baseEncoders: List<BaseEncoder<*>>, concreteClasses: List<KClass<*>>, name: String = "generatedBinarySerializer"
): String = writer {
    write("""
        @Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public fun $name(
            baseEncoders: List<${BaseEncoder::class.qualifiedName}<*>>
        ): ${BinarySerializer::class.qualifiedName} =
            ${BinarySerializer::class.qualifiedName}(baseEncoders + listOf(
    """)
    val baseEncoderTypes = baseEncoders.map { it.type }
    concreteClasses.withIndex().forEach { (classIndex, klass) ->
        write("""
            ${ClassEncoder::class.qualifiedName}(${klass.qualifiedName}::class, // ${classIndex + baseEncoders.size + FirstEncoderId}
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
            { r ->
        """, 3)
        write("""
            val i = r.created(${klass.qualifiedName}(
        """, 4)
        fun cast(p: MetaProperty) = if (p.property.returnType.needsCast()) " as ${p.property.returnType}" else ""
        metaClass.parameterProperties.withIndex().forEach { (pIndex, p) ->
            write("r.read${p.kind}(${p.encoderId()})${cast(p)}${separator(pIndex, metaClass.parameterProperties)}", 5)
        }
        write("""
            ))
        """, 4)
        metaClass.bodyProperties.forEach {
            write("i.${it.property.name} = r.read${it.kind}(${it.encoderId()})${cast(it)}", 4)
        }
        write("""
                    i
                }
            )${separator(classIndex, concreteClasses)}
        """, 2)
    }
    write("""
        ))
    """, 1)
}

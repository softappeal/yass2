package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.*
import kotlin.reflect.*

fun generateBinarySerializer(
    baseEncoders: List<BaseEncoder<*>>, concreteClasses: List<KClass<*>>, name: String = "generatedBinarySerializer"
): String = writer {
    write("""
        @Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection")
        fun $name(
            baseEncoders: List<${BaseEncoder::class.qualifiedName}<*>>
        ) = ${BinarySerializer::class.qualifiedName}(baseEncoders + listOf(
    """)
    val baseEncoderTypes = baseEncoders.map { it.type }
    concreteClasses.withIndex().forEach { (classIndex, klass) ->
        write("""
            ${ClassEncoder::class.qualifiedName}(${klass.qualifiedName}::class, // ${classIndex + baseEncoders.size + FirstEncoderId}
        """, 1)
        val metaClass = klass.metaClass(baseEncoderTypes, concreteClasses)
        val properties = metaClass.properties
        fun MetaProperty.encoderId(tail: String = ""): String = if (kind != PropertyKind.WithId) encoderId.toString() + tail else ""
        if (properties.isEmpty()) {
            write("{ _, _ -> },", 2)
        } else {
            write("""
                { w, i ->
            """, 2)
            properties.forEach { write("w.write${it.kind}(${it.encoderId(", ")}i.${it.property.name})", 3) }
            write("""
                },
            """, 2)
        }
        write("""
            {${if (properties.isEmpty()) "" else " r ->"}
        """, 2)
        properties.forEach {
            val cast = if (it.property.returnType.needsCast()) " as ${it.property.returnType}" else ""
            write("val p${it.property.name} = r.read${it.kind}(${it.encoderId()})$cast", 3)
        }
        write("""
            val i = ${klass.qualifiedName}(${metaClass.parameterNames.joinToString(", ") { "p$it" }})
        """, 3)
        properties.filter { it.property.name !in metaClass.parameterNames }.forEach {
            write("i.${it.property.name} = p${it.property.name}", 3)
        }
        write("""
                    i
                }
            )${separator(classIndex, concreteClasses)}
        """, 1)
    }
    write("""
        ))
    """)
}

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import ch.softappeal.yass2.serialize.binary.*
import ch.softappeal.yass2.serialize.binary.reflect.*
import kotlin.reflect.*

public fun generateBinarySerializer(
    @UnspecifiedInitializationOrder(workaround = "supplier") baseEncodersSupplier: () -> List<BaseEncoder<*>>,
    concreteClasses: List<KClass<*>> = emptyList(),
    name: String = "generatedBinarySerializer",
): String = writer {
    val baseEncoders = baseEncodersSupplier()
    require(
        (baseEncoders.map { it.type }.toSet() + concreteClasses.toSet()).size == (baseEncoders.size + concreteClasses.size)
    ) { "duplicated types" }
    write("""
        @Suppress("UNCHECKED_CAST", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public fun $name(
            baseEncodersSupplier: () -> List<${BaseEncoder::class.qualifiedName}<*>>,
        ): ${BinarySerializer::class.qualifiedName} =
            ${BinarySerializer::class.qualifiedName}(baseEncodersSupplier() + listOf(
    """)
    val baseEncoderTypes = baseEncoders.map { it.type }
    concreteClasses.forEach { klass ->
        write("""
            ${ClassEncoder::class.qualifiedName}(${klass.qualifiedName}::class,
        """, 2)
        val metaClass = klass.metaClass(baseEncoderTypes, concreteClasses)
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
            {${if (metaClass.properties.isNotEmpty()) " r ->" else ""}
        """, 3)
        write("""
            val i = ${klass.qualifiedName}(
        """, 4)
        fun cast(p: MetaProperty) = if (p.property.returnType.needsCast()) " as ${p.property.returnType}" else ""
        metaClass.parameterProperties.forEach { write("r.read${it.kind}(${it.encoderId()})${cast(it)},", 5) }
        write("""
            )
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
    write("""
        ))
    """, 1)
}

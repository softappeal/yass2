package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder
import ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder
import ch.softappeal.yass2.serialize.utf8.EnumUtf8Encoder
import ch.softappeal.yass2.serialize.utf8.Utf8Encoder
import ch.softappeal.yass2.serialize.utf8.Utf8Serializer
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters

private fun KClass<*>.isEnum() = java.isEnum

private fun checkNotEnum(classes: List<KClass<*>>) {
    classes.firstOrNull { it.isEnum() }?.let { klass -> error("enum class ${klass.qualifiedName} belongs to enumClasses") }
}

private fun List<KClass<*>>.checkNotDuplicated() {
    require(hasNoDuplicates()) { "classes ${duplicates().map { it.qualifiedName }} are duplicated" }
}

private fun KClass<*>.getAllPropertiesNotThrowable() = memberProperties
    .filterNot { (it.name == "cause" || it.name == "message") && isSubclassOf(Throwable::class) }
    .sortedBy { it.name }

private data class Classes(
    val baseClasses: List<KClass<*>>,
    val enumClasses: List<KClass<out Enum<*>>>,
    val concreteClasses: List<KClass<*>>,
)

private fun getClasses(encoderClasses: List<KClass<*>>, concreteAndEnumClasses: List<KClass<*>>): Classes {
    val encoderTypes = encoderClasses.map { it.supertypes.first().arguments.first().type!!.classifier as KClass<*> }
    val enumClasses = concreteAndEnumClasses.filter { it.isEnum() }.map { @Suppress("UNCHECKED_CAST") (it as KClass<Enum<*>>) }
    val concreteClasses = concreteAndEnumClasses.filterNot { it.isEnum() }
    val baseClasses = encoderTypes + enumClasses
    (baseClasses + concreteClasses).checkNotDuplicated()
    checkNotEnum(encoderTypes + concreteClasses)
    return Classes(baseClasses, enumClasses, concreteClasses)
}

private abstract class Property(val property: KProperty1<out Any, *>)

/**
 * Concrete classes must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 */
private class Properties<P : Property>(klass: KClass<*>, createProperty: (property: KProperty1<out Any, *>) -> P) {
    val parameter: List<P>
    val body: List<P>
    val all: List<P>

    init {
        require(!klass.isAbstract) { "class ${klass.qualifiedName} must be concrete" }
        val properties = klass.getAllPropertiesNotThrowable().map { createProperty(it) }
        parameter = buildList {
            val primaryConstructor = klass.primaryConstructor ?: error(
                "class ${klass.qualifiedName} must hava a primary constructor"
            )
            val propertyNames = properties.map { it.property.name }
            val parameterNames = primaryConstructor.valueParameters.map { it.name }
            parameterNames.forEach { parameterName ->
                require(propertyNames.contains(parameterName)) {
                    "primary constructor parameter $parameterName of class ${klass.qualifiedName} must be a property"
                }
                add(properties.first { it.property.name == parameterName })
            }
        }
        body = buildList {
            properties
                .filter { it !in parameter }
                .forEach { property ->
                    require(property.property is KMutableProperty1<out Any, *>) {
                        "body property ${property.property.name} of ${klass.qualifiedName} must be var"
                    }
                    add(property)
                }
        }
        all = parameter + body
    }
}

private enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

public fun CodeWriter.generateBinarySerializer(
    encoderClasses: List<KClass<out BinaryEncoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderClasses, concreteAndEnumClasses)

    class BinaryProperty(property: KProperty1<out Any, *>) : Property(property) {
        var kind: PropertyKind
        val encoderId: Int

        init {
            val type = property.returnType.classifier as KClass<*>
            kind = if (property.returnType.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
            if (type == List::class) encoderId = BinarySerializer.LIST_ENCODER_ID else {
                val baseClassIndex = baseClasses.indexOfFirst { it == type }
                if (baseClassIndex >= 0) encoderId = baseClassIndex + BinarySerializer.FIRST_ENCODER_ID else {
                    fun KClass<*>.hasSuperCLass(superClass: KClass<*>) = superclasses.contains(superClass)
                    val concreteClassIndex = concreteClasses.indexOfFirst { it == type }
                    if (concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperCLass(concreteClasses[concreteClassIndex]) }) {
                        encoderId = concreteClassIndex + baseClasses.size + BinarySerializer.FIRST_ENCODER_ID
                    } else {
                        kind = PropertyKind.WithId
                        encoderId = 0 // not used for WithId
                    }
                }
            }
        }
    }

    writeLine()
    writeNestedLine("public fun createBinarySerializer(): ${BinarySerializer::class.qualifiedName} =") {
        writeNestedLine("object : ${BinarySerializer::class.qualifiedName}() {", "}") {
            writeNestedLine("init {", "}") {
                writeNestedLine("initialize(", ")") {
                    encoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
                    enumClasses.forEach { type ->
                        writeNestedLine("${EnumBinaryEncoder::class.qualifiedName}(", "),") {
                            writeNestedLine("${type.qualifiedName}::class, enumValues(),")
                        }
                    }
                    fun BinaryProperty.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
                    concreteClasses.forEach { type ->
                        writeNestedLine("${BinaryEncoder::class.qualifiedName}(", "),") {
                            writeNestedLine("${type.qualifiedName}::class,")
                            val properties = Properties(type) { BinaryProperty(it) }
                            writeNestedLine("{ i ->", "},") {
                                properties.all.forEach { property ->
                                    writeNestedLine("write${property.kind}(${property.encoderId(", ")}i.${property.property.name})")
                                }
                            }
                            writeNestedLine("{", "}") {
                                writeNestedLine("val i = ${type.qualifiedName}(", ")") {
                                    properties.parameter.forEach { property ->
                                        writeNestedLine("read${property.kind}(${property.encoderId()}) as ${property.property.returnType.convert()},")
                                    }
                                }
                                properties.body.forEach { property ->
                                    writeNestedLine("i.${property.property.name} = read${property.kind}(${property.encoderId()}) as ${property.property.returnType.convert()}")
                                }
                                writeNestedLine("i")
                            }
                        }
                    }
                }
            }
        }
    }
}

public fun CodeWriter.generateUtf8Encoders(
    encoderClasses: List<KClass<out Utf8Encoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderClasses, concreteAndEnumClasses)

    class Utf8Property(property: KProperty1<out Any, *>) : Property(property) {
        val encoderId = when (val type = property.returnType.classifier as KClass<*>) {
            List::class -> Utf8Serializer.LIST_ENCODER_ID
            String::class -> Utf8Serializer.STRING_ENCODER_ID
            else -> {
                val baseClassIndex = baseClasses.indexOfFirst { it == type }
                if (baseClassIndex >= 0) baseClassIndex + Utf8Serializer.FIRST_ENCODER_ID else Utf8Serializer.NO_ENCODER_ID
            }
        }

        fun withId() = encoderId == Utf8Serializer.NO_ENCODER_ID
        fun hasId() =
            encoderId != Utf8Serializer.NO_ENCODER_ID && encoderId != Utf8Serializer.STRING_ENCODER_ID && encoderId != Utf8Serializer.LIST_ENCODER_ID
    }

    writeLine()
    writeNestedLine(
        "public fun createUtf8Encoders(): ${List::class.qualifiedName}<${Utf8Encoder::class.qualifiedName}<*>> = listOf(",
        ")",
    ) {
        encoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
        enumClasses.forEach { type ->
            writeNestedLine("${EnumUtf8Encoder::class.qualifiedName}(", "),") {
                writeNestedLine("${type.qualifiedName}::class,")
                writeNestedLine("${type.qualifiedName}::valueOf,")
            }
        }
        concreteClasses.forEach { type ->
            writeNestedLine("${ClassUtf8Encoder::class.qualifiedName}(", "),") {
                writeNestedLine("${type.qualifiedName}::class,")
                val properties = Properties(type) { Utf8Property(it) }
                writeNestedLine("{ i ->", "},") {
                    properties.all.forEach {
                        writeNestedLine("write${if (it.withId()) "With" else "No"}Id(\"${it.property.name}\",${if (it.withId()) "" else " ${it.encoderId},"} i.${it.property.name})")
                    }
                }
                writeNestedLine("{", "},") {
                    writeNestedLine("val i = ${type.qualifiedName}(", ")") {
                        properties.parameter.forEach { property ->
                            writeNestedLine("getProperty(\"${property.property.name}\") as ${property.property.returnType.convert()},")
                        }
                    }
                    properties.body.forEach { property ->
                        writeNestedLine("i.${property.property.name} = getProperty(\"${property.property.name}\") as ${property.property.returnType.convert()}")
                    }
                    writeNestedLine("i")
                }
                properties.all.forEach { property ->
                    writeNestedLine("\"${property.property.name}\" to ${if (property.hasId()) property.encoderId else Utf8Serializer.NO_ENCODER_ID},")
                }
            }
        }
    }
}

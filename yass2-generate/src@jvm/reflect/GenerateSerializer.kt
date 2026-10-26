@file:OptIn(InternalSerializeApi::class)
@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.generate.writeFun
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.serialize.InternalSerializeApi
import ch.softappeal.yass2.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder
import ch.softappeal.yass2.serialize.string.BaseStringEncoder
import ch.softappeal.yass2.serialize.string.ClassStringEncoder
import ch.softappeal.yass2.serialize.string.EnumStringEncoder
import ch.softappeal.yass2.serialize.string.StringEncoder
import ch.softappeal.yass2.serialize.string.StringSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters

private data class Classes(
    val baseClasses: List<KClass<*>>,
    val enumClasses: List<KClass<out Enum<*>>>,
    val concreteClasses: List<KClass<*>>,
)

private fun KClass<*>.isEnum() = java.isEnum

private fun getClasses(encoders: List<KClass<*>>, concreteAndEnumClasses: List<KClass<*>>): Classes {
    val encoderTypes = encoders
        .map { it.supertypes.first().arguments.first().type!!.classifier as KClass<*> }
    val enumClasses = concreteAndEnumClasses.filter { it.isEnum() }.map { @Suppress("UNCHECKED_CAST") (it as KClass<Enum<*>>) }
    val concreteClasses = concreteAndEnumClasses.filterNot { it.isEnum() }
    val baseClasses = encoderTypes + enumClasses
    (baseClasses + concreteClasses).apply {
        require(hasNoDuplicates()) { "classes ${duplicates().map { it.qualifiedName }} are duplicated" }
    }
    (encoderTypes + concreteClasses)
        .firstOrNull { it.isEnum() }
        ?.let { error("enum class '${it.qualifiedName}' belongs to '${ConcreteAndEnumClasses::class.qualifiedName}'") }
    return Classes(baseClasses, enumClasses, concreteClasses)
}

private abstract class Property(property: KProperty1<out Any, *>) {
    val name = property.name
    val returnType = property.returnType

    protected val nullable = returnType.isMarkedNullable

    protected val classifier = returnType.classifier
}

private fun <P : Property> KClass<*>.properties(createProperty: (property: KProperty1<out Any, *>) -> P): List<P> {
    require(!isAbstract) { "class '$qualifiedName' must be concrete" }
    val properties = memberProperties
        .filterNot { (it.name == "cause" || it.name == "message") && isSubclassOf(Throwable::class) }
        .filter { it.visibility == KVisibility.PUBLIC }
        .map { createProperty(it) }
    val parameters = (primaryConstructor ?: error("class '$qualifiedName' must have a primary constructor")).valueParameters
    val constructorProperties = parameters.map { parameter ->
        properties
            .firstOrNull { it.name == parameter.name }
            ?: error("primary constructor parameter '${parameter.name}' of class '$qualifiedName' must be a public property")
    }
    require(properties.all { it in constructorProperties }) { "class '$qualifiedName' must not have body properties" }
    return constructorProperties
}

internal fun CodeWriter.generateBinarySerializer(
    encoders: List<KClass<out BinaryEncoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoders, concreteAndEnumClasses)

    class BinaryProperty(property: KProperty1<out Any, *>) : Property(property) {
        private val encoderId = if (classifier == List::class) BinarySerializer.LIST_ENCODER_ID else {
            val baseClassIndex = baseClasses.indexOfFirst { it == classifier }
            if (baseClassIndex >= 0) baseClassIndex + BinarySerializer.FIRST_ENCODER_ID else {
                fun KClass<*>.hasSuperCLass(superClass: KClass<*>) =
                    superclasses.contains(superClass)

                val concreteClassIndex = concreteClasses.indexOfFirst { it == classifier }
                if (
                    concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperCLass(concreteClasses[concreteClassIndex]) }
                ) concreteClassIndex + baseClasses.size + BinarySerializer.FIRST_ENCODER_ID else BinarySerializer.NO_ENCODER_ID
            }
        }

        private fun suffix() =
            if (encoderId == BinarySerializer.NO_ENCODER_ID) "Object" else if (nullable) "Optional" else "Required"

        fun readObject() = "read${suffix()}(${if (encoderId == BinarySerializer.NO_ENCODER_ID) "" else encoderId})"
        fun writeObject(reference: String) =
            "write${suffix()}($reference${if (encoderId == BinarySerializer.NO_ENCODER_ID) "" else ", $encoderId"})"
    }

    writeFun("binarySerializer(): ${BinarySerializer::class.qualifiedName}") {
        writeNestedLine("object : ${BinarySerializer::class.qualifiedName}() {", "}") {
            writeNestedLine("init {", "}") {
                writeNestedLine("initialize(", ")") {
                    writeNestedLine("// ${List::class.qualifiedName}: ${BinarySerializer.LIST_ENCODER_ID}")
                    var encoderId = BinarySerializer.FIRST_ENCODER_ID
                    encoders.forEach { type -> writeNestedLine("${type.qualifiedName}, // ${encoderId++}") }
                    enumClasses.forEach { type ->
                        writeNestedLine("${EnumBinaryEncoder::class.qualifiedName}(", "),") {
                            writeNestedLine("${type.qualifiedName}::class, kotlin.enums.enumEntries(), // ${encoderId++}")
                        }
                    }
                    concreteClasses.forEach { type ->
                        writeNestedLine("${BinaryEncoder::class.qualifiedName}(", "),") {
                            writeNestedLine("${type.qualifiedName}::class, // ${encoderId++}")
                            val properties = type.properties { BinaryProperty(it) }
                            writeNestedLine("{ i ->", "},") {
                                properties.forEach { writeNestedLine(it.writeObject("i.${it.name}")) }
                            }
                            writeNestedLine("{", "}") {
                                writeNestedLine("${type.qualifiedName}(", ")") {
                                    properties.forEach { writeNestedLine("${it.name} = ${it.readObject()} as ${it.returnType.toType()},") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun CodeWriter.generateStringEncoders(
    encoders: List<KClass<out BaseStringEncoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoders, concreteAndEnumClasses)

    class StringProperty(property: KProperty1<out Any, *>) : Property(property) {
        private val encoderId = when (classifier) {
            String::class -> StringSerializer.STRING_ENCODER_ID
            Boolean::class -> StringSerializer.BOOLEAN_ENCODER_ID
            List::class -> StringSerializer.LIST_ENCODER_ID
            else -> {
                val baseClassIndex = baseClasses.indexOfFirst { it == classifier }
                if (baseClassIndex >= 0) baseClassIndex + StringSerializer.FIRST_ENCODER_ID else StringSerializer.NO_ENCODER_ID
            }
        }

        fun writeProperty(reference: String) =
            "writeProperty(\"$name\", $reference${if (encoderId == StringSerializer.NO_ENCODER_ID) "" else ", $encoderId"})"

        fun propertyEncoderId() = "\"$name\" to ${
            if (
                encoderId != StringSerializer.STRING_ENCODER_ID &&
                encoderId != StringSerializer.BOOLEAN_ENCODER_ID &&
                encoderId != StringSerializer.LIST_ENCODER_ID
            ) encoderId else StringSerializer.NO_ENCODER_ID
        }"
    }

    writeFun("stringEncoders(): List<${StringEncoder::class.qualifiedName}<*>>") {
        writeNestedLine("listOf(", ")") {
            writeNestedLine("// ${String::class.qualifiedName}: ${StringSerializer.STRING_ENCODER_ID}")
            writeNestedLine("// ${Boolean::class.qualifiedName}: ${StringSerializer.BOOLEAN_ENCODER_ID}")
            writeNestedLine("// ${List::class.qualifiedName}: ${StringSerializer.LIST_ENCODER_ID}")
            var encoderId = StringSerializer.FIRST_ENCODER_ID
            encoders.forEach { type -> writeNestedLine("${type.qualifiedName}, // ${encoderId++}") }
            enumClasses.forEach { type ->
                writeNestedLine("${EnumStringEncoder::class.qualifiedName}(", "),") {
                    writeNestedLine("${type.qualifiedName}::class, // ${encoderId++}")
                    writeNestedLine("${type.qualifiedName}::valueOf,")
                }
            }
            concreteClasses.forEach { type ->
                writeNestedLine("${ClassStringEncoder::class.qualifiedName}(", "),") {
                    writeNestedLine("${type.qualifiedName}::class, // ${encoderId++}")
                    val properties = type.properties { StringProperty(it) }
                    writeNestedLine("{ i ->", "},") {
                        properties.forEach { writeNestedLine(it.writeProperty("i.${it.name}")) }
                    }
                    writeNestedLine("{", "},") {
                        writeNestedLine("${type.qualifiedName}(", ")") {
                            properties.forEach { writeNestedLine("getProperty(\"${it.name}\") as ${it.returnType.toType()},") }
                        }
                    }
                    properties.forEach { writeNestedLine("${it.propertyEncoderId()},") }
                }
            }
        }
    }
}

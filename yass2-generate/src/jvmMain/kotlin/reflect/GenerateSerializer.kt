@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.PropertyKind
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.serialize.binary.BINARY_FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.BINARY_LIST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder
import ch.softappeal.yass2.serialize.text.ClassTextEncoder
import ch.softappeal.yass2.serialize.text.EnumTextEncoder
import ch.softappeal.yass2.serialize.text.TEXT_FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.text.TEXT_LIST_ENCODER_ID
import ch.softappeal.yass2.serialize.text.TEXT_STRING_ENCODER_ID
import ch.softappeal.yass2.serialize.text.TextEncoder
import ch.softappeal.yass2.serialize.text.TextSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters

private fun checkNotEnum(classes: List<KClass<*>>) {
    classes.firstOrNull { it.isEnum() }?.let { klass -> error("enum class ${klass.qualifiedName} belongs to enumClasses") }
}

private fun List<KClass<*>>.checkNotDuplicated() {
    require(hasNoDuplicates()) { "classes ${duplicates().map { it.qualifiedName }} are duplicated" }
}

private fun KClass<*>.getAllPropertiesNotThrowable() = memberProperties
    .filterNot { (it.name == "cause" || it.name == "message") && isSubclassOf(Throwable::class) }
    .sortedBy { it.name }

private fun getBaseClasses(
    encoderClasses: List<KClass<*>>,
    enumClasses: List<KClass<out Enum<*>>>,
    concreteClasses: List<KClass<*>>,
): List<KClass<*>> {
    val encoderTypes = encoderClasses.map { it.supertypes.first().arguments.first().type!!.classifier as KClass<*> }
    val baseClasses = encoderTypes + enumClasses
    (baseClasses + concreteClasses).checkNotDuplicated()
    checkNotEnum(encoderTypes + concreteClasses)
    return baseClasses
}

public fun CodeWriter.generateSerializer(
    binaryEncoderClasses: List<KClass<out BinaryEncoder<*>>>,
    textEncoderClasses: List<KClass<out TextEncoder<*>>>,
    enumClasses: List<KClass<out Enum<*>>>,
    concreteClasses: List<KClass<*>>,
) {
    abstract class Property(val property: KProperty1<out Any, *>)

    class Properties<P : Property>(klass: KClass<*>, createProperty: (property: KProperty1<out Any, *>) -> P) {
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
                val parameterNames = primaryConstructor.valueParameters.map { it.name }
                val propertyNames = properties.map { it.property.name }
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

    if (binaryEncoderClasses.isNotEmpty()) {
        val baseClasses = getBaseClasses(binaryEncoderClasses, enumClasses, concreteClasses)

        class BinaryProperty(property: KProperty1<out Any, *>) : Property(property) {
            var kind: PropertyKind
            var encoderId: Int = -1

            init {
                val type = property.returnType.classifier as KClass<*>
                kind = if (property.returnType.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
                if (type == List::class) encoderId = BINARY_LIST_ENCODER_ID else {
                    val baseClassIndex = baseClasses.indexOfFirst { it == type }
                    if (baseClassIndex >= 0) encoderId = baseClassIndex + BINARY_FIRST_ENCODER_ID else {
                        fun KClass<*>.hasSuperCLass(superClass: KClass<*>) = superclasses.contains(superClass)
                        val concreteClassIndex = concreteClasses.indexOfFirst { it == type }
                        if (concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperCLass(concreteClasses[concreteClassIndex]) }) {
                            encoderId = concreteClassIndex + baseClasses.size + BINARY_FIRST_ENCODER_ID
                        } else kind = PropertyKind.WithId
                    }
                }
            }
        }

        writeLine()
        writeNestedLine("public fun createBinarySerializer(): ${BinarySerializer::class.qualifiedName} =") {
            writeNestedLine("object : ${BinarySerializer::class.qualifiedName}() {", "}") {
                writeNestedLine("init {", "}") {
                    writeNestedLine("initialize(", ")") {
                        binaryEncoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
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
                                            writeNestedLine("read${property.kind}(${property.encoderId()}) as ${property.property.returnType},")
                                        }
                                    }
                                    properties.body.forEach { property ->
                                        writeNestedLine("i.${property.property.name} = read${property.kind}(${property.encoderId()}) as ${property.property.returnType}")
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

    if (textEncoderClasses.isNotEmpty()) {
        val baseClasses = getBaseClasses(textEncoderClasses, enumClasses, concreteClasses)

        class TextProperty(property: KProperty1<out Any, *>) : Property(property) {
            val encoderId = when (val type = property.returnType.classifier as KClass<*>) {
                List::class -> TEXT_LIST_ENCODER_ID
                String::class -> TEXT_STRING_ENCODER_ID
                else -> {
                    val baseClassIndex = baseClasses.indexOfFirst { it == type }
                    if (baseClassIndex >= 0) baseClassIndex + TEXT_FIRST_ENCODER_ID else -1
                }
            }

            fun needsTo() = encoderId >= 0 && encoderId != TEXT_STRING_ENCODER_ID && encoderId != TEXT_LIST_ENCODER_ID
            fun withId() = encoderId < 0
        }

        writeLine()
        writeNestedLine("public fun createTextSerializer(multilineWrite: kotlin.Boolean): ${TextSerializer::class.qualifiedName} =") {
            writeNestedLine("object : ${TextSerializer::class.qualifiedName}(multilineWrite) {", "}") {
                writeNestedLine("init {", "}") {
                    writeNestedLine("initialize(", ")") {
                        textEncoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
                        enumClasses.forEach { type ->
                            writeNestedLine("${EnumTextEncoder::class.qualifiedName}(", "),") {
                                writeNestedLine("${type.qualifiedName}::class,")
                                writeNestedLine("${type.qualifiedName}::valueOf,")
                            }
                        }
                        concreteClasses.forEach { type ->
                            writeNestedLine("${ClassTextEncoder::class.qualifiedName}(", "),") {
                                writeNestedLine("${type.qualifiedName}::class,")
                                val properties = Properties(type) { TextProperty(it) }
                                writeNestedLine("{ i ->", "},") {
                                    properties.all.forEach {
                                        writeNestedLine("write${if (it.withId()) "With" else "No"}Id(\"${it.property.name}\",${if (it.withId()) "" else " ${it.encoderId},"} i.${it.property.name})")
                                    }
                                }
                                writeNestedLine("{", "},") {
                                    writeNestedLine("val i = ${type.qualifiedName}(", ")") {
                                        properties.parameter.forEach { property ->
                                            writeNestedLine("getProperty(\"${property.property.name}\") as ${property.property.returnType},")
                                        }
                                    }
                                    properties.body.forEach { property ->
                                        writeNestedLine("i.${property.property.name} = getProperty(\"${property.property.name}\") as ${property.property.returnType}")
                                    }
                                    writeNestedLine("i")
                                }
                                properties.all.forEach { property ->
                                    if (property.needsTo()) writeNestedLine("\"${property.property.name}\" to ${property.encoderId},")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

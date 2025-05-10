@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.NotJsPlatform
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BINARY_FIRST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BINARY_LIST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BINARY_NO_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.BinarySerializer
import ch.softappeal.yass2.core.serialize.binary.EnumBinaryEncoder
import ch.softappeal.yass2.core.serialize.string.BaseStringEncoder
import ch.softappeal.yass2.core.serialize.string.ClassStringEncoder
import ch.softappeal.yass2.core.serialize.string.DoubleStringEncoder
import ch.softappeal.yass2.core.serialize.string.EnumStringEncoder
import ch.softappeal.yass2.core.serialize.string.STRING_BOOLEAN_ENCODER_ID
import ch.softappeal.yass2.core.serialize.string.STRING_FIRST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.string.STRING_LIST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.string.STRING_NO_ENCODER_ID
import ch.softappeal.yass2.core.serialize.string.STRING_STRING_ENCODER_ID
import ch.softappeal.yass2.core.serialize.string.StringEncoder
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
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

private fun getClasses(encoderObjects: List<KClass<*>>, concreteAndEnumClasses: List<KClass<*>>): Classes {
    val encoderTypes = encoderObjects.map {
        it.supertypes.first().arguments.first().type!!.classifier as KClass<*>
    }
    val enumClasses = concreteAndEnumClasses.filter { it.isEnum() }.map { @Suppress("UNCHECKED_CAST") (it as KClass<Enum<*>>) }
    val concreteClasses = concreteAndEnumClasses.filterNot { it.isEnum() }
    val baseClasses = encoderTypes + enumClasses
    (baseClasses + concreteClasses).apply {
        require(hasNoDuplicates()) { "classes ${duplicates().map { it.qualifiedName }} are duplicated" }
    }
    (encoderTypes + concreteClasses).firstOrNull { it.isEnum() }
        ?.let { error("enum class ${it.qualifiedName} belongs to concreteAndEnumClasses") }
    return Classes(baseClasses, enumClasses, concreteClasses)
}

private abstract class Property(property: KProperty1<out Any, *>) {
    val name = property.name
    val returnType = property.returnType
    val mutable = property is KMutableProperty1<out Any, *>


    protected val nullable = returnType.isMarkedNullable
    protected val classifier = returnType.classifier

}

/** see [ConcreteAndEnumClasses] */
private class Properties<P : Property>(klass: KClass<*>, createProperty: (property: KProperty1<out Any, *>) -> P) {

    val parameter: List<P>
    val body: List<P>
    val all: List<P>

    init {
        require(!klass.isAbstract) { "class ${klass.qualifiedName} must be concrete" }
        val properties = klass.memberProperties
            .filterNot { (it.name == "cause" || it.name == "message") && klass.isSubclassOf(Throwable::class) }

            .sortedBy { it.name }
            .map { createProperty(it) }
        parameter = buildList {
            val primaryConstructor =
                klass.primaryConstructor ?: error("class ${klass.qualifiedName} must hava a primary constructor")
            val propertyNames = properties.map { it.name }
            val parameterNames = primaryConstructor.valueParameters.map { it.name }
            parameterNames.forEach { parameterName ->
                require(propertyNames.contains(parameterName)) {
                    "primary constructor parameter $parameterName of class ${klass.qualifiedName} must be a property"
                }
                add(properties.first { it.name == parameterName })
            }
        }
        body = buildList {
            properties
                .filter { it !in parameter }
                .forEach { property ->
                    require(property.mutable) {
                        "body property ${property.name} of ${klass.qualifiedName} must be var"
                    }
                    add(property)
                }
        }
        all = parameter + body
    }
}

public fun CodeWriter.generateBinarySerializer(
    encoderObjects: List<KClass<out BinaryEncoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)

    @OptIn(InternalApi::class)
    class BinaryProperty(property: KProperty1<out Any, *>) : Property(property) {
        private val encoderId = if (classifier == List::class) BINARY_LIST_ENCODER_ID else {
            val baseClassIndex = baseClasses.indexOfFirst { it == classifier }
            if (baseClassIndex >= 0) baseClassIndex + BINARY_FIRST_ENCODER_ID else {
                fun KClass<*>.hasSuperCLass(superClass: KClass<*>) =
                    superclasses.contains(superClass)

                val concreteClassIndex = concreteClasses.indexOfFirst { it == classifier }
                if (
                    concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperCLass(concreteClasses[concreteClassIndex]) }
                ) concreteClassIndex + baseClasses.size + BINARY_FIRST_ENCODER_ID else BINARY_NO_ENCODER_ID
            }
        }

        private fun suffix() = if (encoderId == BINARY_NO_ENCODER_ID) "Object" else if (nullable) "Optional" else "Required"
        fun readObject() = "read${suffix()}(${if (encoderId == BINARY_NO_ENCODER_ID) "" else encoderId})"
        fun writeObject(reference: String) =
            "write${suffix()}($reference${if (encoderId == BINARY_NO_ENCODER_ID) "" else ", $encoderId"})"
    }

    writeLine()
    writeNestedLine("public object BinarySerializer : ${BinarySerializer::class.qualifiedName}() {", "}") {
        writeNestedLine("init {", "}") {
            writeNestedLine("initialize(", ")") {
                encoderObjects.forEach { type ->
                    writeNestedLine("${type.qualifiedName},")
                }
                enumClasses.forEach { type ->
                    writeNestedLine("${EnumBinaryEncoder::class.qualifiedName}(", "),") {
                        writeNestedLine("${type.qualifiedName}::class, enumValues(),")
                    }
                }
                concreteClasses.forEach { type ->
                    writeNestedLine("${BinaryEncoder::class.qualifiedName}(", "),") {
                        writeNestedLine("${type.qualifiedName}::class,")
                        val properties = Properties(type) { BinaryProperty(it) }
                        writeNestedLine("{ i ->", "},") {
                            properties.all.forEach { property ->
                                writeNestedLine(property.writeObject("i.${property.name}"))
                            }
                        }
                        writeNestedLine("{", "}") {
                            fun BinaryProperty.readObjectWithCast() = "${readObject()} as ${returnType.toType()}"
                            writeNestedLine(
                                "${type.qualifiedName}(",
                                ")${if (properties.body.isEmpty()) "" else ".apply {"}",
                            ) {
                                properties.parameter.forEach { property ->
                                    writeNestedLine("${property.readObjectWithCast()},")
                                }
                            }
                            if (properties.body.isNotEmpty()) {
                                nested {
                                    properties.body.forEach { property ->
                                        writeNestedLine("${property.name} = ${property.readObjectWithCast()}")
                                    }
                                }
                                writeNestedLine("}")
                            }
                        }
                    }
                }
            }
        }
    }
}

public fun CodeWriter.generateStringEncoders(
    encoderObjects: List<KClass<out BaseStringEncoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)

    @OptIn(InternalApi::class)
    class StringProperty(property: KProperty1<out Any, *>) : Property(property) {
        private val encoderId = when (classifier) {
            String::class -> STRING_STRING_ENCODER_ID
            Boolean::class -> STRING_BOOLEAN_ENCODER_ID
            List::class -> STRING_LIST_ENCODER_ID
            else -> {
                val baseClassIndex = baseClasses.indexOfFirst { it == classifier }
                if (baseClassIndex >= 0) baseClassIndex + STRING_FIRST_ENCODER_ID else STRING_NO_ENCODER_ID
            }
        }

        fun writeProperty(reference: String) =
            "writeProperty(\"$name\", $reference${if (encoderId == STRING_NO_ENCODER_ID) "" else ", $encoderId"})"

        fun propertyEncoderId() = "\"$name\" to ${
            if (
                encoderId != STRING_NO_ENCODER_ID &&
                encoderId != STRING_STRING_ENCODER_ID &&
                encoderId != STRING_BOOLEAN_ENCODER_ID &&
                encoderId != STRING_LIST_ENCODER_ID
            ) encoderId else STRING_NO_ENCODER_ID
        }"
    }

    writeLine()
    writeNestedLine("public val StringEncoders: List<${StringEncoder::class.qualifiedName}<*>> = listOf(", ")") {
        encoderObjects.forEach { type ->
            @OptIn(NotJsPlatform::class)
            if (type == DoubleStringEncoder::class) writeNestedLine("@OptIn(${NotJsPlatform::class.qualifiedName}::class)")
            writeNestedLine("${type.qualifiedName},")
        }
        enumClasses.forEach { type ->
            writeNestedLine("${EnumStringEncoder::class.qualifiedName}(", "),") {
                writeNestedLine("${type.qualifiedName}::class,")
                writeNestedLine("${type.qualifiedName}::valueOf,")
            }
        }
        concreteClasses.forEach { type ->
            writeNestedLine("${ClassStringEncoder::class.qualifiedName}(", "),") {
                val properties = Properties(type) { StringProperty(it) }
                writeNestedLine("${type.qualifiedName}::class, ${properties.body.isNotEmpty()},")
                writeNestedLine("{ i ->", "},") {
                    fun List<StringProperty>.forEach() = forEach { property ->
                        writeNestedLine(property.writeProperty("i.${property.name}"))
                    }
                    properties.parameter.forEach()
                    if (properties.body.isNotEmpty()) writeNestedLine("startBodyProperties()")
                    properties.body.forEach()
                }
                writeNestedLine("{", "},") {
                    fun StringProperty.getPropertyWithCast() = "getProperty(\"$name\") as ${returnType.toType()}"
                    writeNestedLine(
                        "${type.qualifiedName}(",
                        ")${if (properties.body.isEmpty()) "" else ".apply {"}",
                    ) {
                        properties.parameter.forEach { property ->
                            writeNestedLine("${property.getPropertyWithCast()},")
                        }
                    }
                    if (properties.body.isNotEmpty()) {
                        nested {
                            properties.body.forEach { property ->
                                writeNestedLine("${property.name} = ${property.getPropertyWithCast()}")
                            }
                        }
                        writeNestedLine("}")
                    }
                }
                properties.all.forEach { property ->
                    writeNestedLine("${property.propertyEncoderId()},")
                }
            }
        }
    }
}

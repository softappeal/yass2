@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.NotJsPlatform
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BINARY_FIRST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BINARY_LIST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BINARY_NO_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.BinarySerializer
import ch.softappeal.yass2.core.serialize.binary.EnumBinaryEncoder
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
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

private data class Classes(
    val baseClasses: List<KSType>,
    val enumClasses: List<KSType>,
    val concreteClasses: List<KSType>,
)

private fun KSType.isEnum() = (declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS

private fun getClasses(encoderObjects: List<KSType>, concreteAndEnumClasses: List<KSType>): Classes {
    val encoderTypes = encoderObjects.map {
        (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve()
    }
    val enumClasses = concreteAndEnumClasses.filter { it.isEnum() }
    val concreteClasses = concreteAndEnumClasses.filterNot { it.isEnum() }
    val baseClasses = encoderTypes + enumClasses
    (baseClasses + concreteClasses).apply {
        require(hasNoDuplicates()) { "classes ${duplicates().map { it.qualifiedName }} are duplicated" }
    }
    (encoderTypes + concreteClasses).firstOrNull { it.isEnum() }
        ?.let { error("enum class ${it.qualifiedName} belongs to ConcreteAndEnumClasses") }
    return Classes(baseClasses, enumClasses, concreteClasses)
}

private abstract class Property(property: KSPropertyDeclaration) {
    val name = property.simpleName.asString()
    val returnType = property.type
    val mutable = property.isMutable
    val parentDeclaration = property.parentDeclaration
    protected val type = returnType.resolve()
    protected val nullable = type.isMarkedNullable
    protected val typeNotNullable = type.makeNotNullable()
    protected val typeQualifiedName = typeNotNullable.qualifiedName
}

/** see [ConcreteAndEnumClasses] */
private class Properties<P : Property>(type: KSType, createProperty: (property: KSPropertyDeclaration) -> P) {
    private val klass = type.declaration as KSClassDeclaration
    val parameter: List<P>
    val body: List<P>
    val all: List<P>

    init {
        require(!klass.isAbstract()) { "class ${klass.qualifiedName()} must be concrete" }
        val properties = klass.getAllProperties().toList()
            .filterNot { (it.name == "cause" || it.name == "message") && ("kotlin.Throwable" == it.parentDeclaration!!.qualifiedName()) }
            .filter { it.isPublic() } // TODO: see https://github.com/google/ksp/issues/2443
            .sortedBy { it.name }
            .map { createProperty(it) }
        parameter = buildList {
            val primaryConstructor =
                klass.primaryConstructor ?: error("class ${klass.qualifiedName()} must hava a primary constructor")
            val parameters = primaryConstructor.parameters

            parameters.forEach { parameter ->
                require(parameter.isVal || parameter.isVar) {
                    "primary constructor parameter ${parameter.name!!.asString()} of class ${klass.qualifiedName()} must be a property"
                }
                add(properties.first { it.name == parameter.name!!.asString() })
            }
        }
        body = buildList {
            properties
                .filter { it !in parameter }
                .forEach { property ->
                    require(property.mutable) {
                        "body property ${property.name} of ${property.parentDeclaration!!.qualifiedName()} must be var"
                    }
                    add(property)
                }
        }
        all = parameter + body
    }
}


internal fun CodeWriter.generateBinarySerializer(
    encoderObjects: List<KSType>,
    concreteAndEnumClasses: List<KSType>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)

    @OptIn(InternalApi::class)
    class BinaryProperty(property: KSPropertyDeclaration) : Property(property) {
        private val encoderId = if (typeQualifiedName == List::class.qualifiedName) BINARY_LIST_ENCODER_ID else {
            val baseClassIndex = baseClasses.indexOfFirst { it == typeNotNullable }
            if (baseClassIndex >= 0) baseClassIndex + BINARY_FIRST_ENCODER_ID else {
                fun KSType.hasSuperCLass(superClass: KSType) =
                    (this.declaration as KSClassDeclaration).superTypes.map { it.resolve() }.contains(superClass)

                val concreteClassIndex = concreteClasses.indexOfFirst { it == typeNotNullable }
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


internal fun CodeWriter.generateStringEncoders(
    encoderObjects: List<KSType>,
    concreteAndEnumClasses: List<KSType>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)

    @OptIn(InternalApi::class)
    class StringProperty(property: KSPropertyDeclaration) : Property(property) {
        private val encoderId = when (typeQualifiedName) {
            String::class.qualifiedName -> STRING_STRING_ENCODER_ID
            Boolean::class.qualifiedName -> STRING_BOOLEAN_ENCODER_ID
            List::class.qualifiedName -> STRING_LIST_ENCODER_ID
            else -> {
                val baseClassIndex = baseClasses.indexOfFirst { it == typeNotNullable }
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
            if (type.qualifiedName == DoubleStringEncoder::class.qualifiedName) writeNestedLine("@OptIn(${NotJsPlatform::class.qualifiedName}::class)")
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

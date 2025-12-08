@file:OptIn(InternalApi::class)
@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.serialize.binary.BINARY_FIRST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BINARY_LIST_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BINARY_NO_ENCODER_ID
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.BinarySerializer
import ch.softappeal.yass2.core.serialize.binary.EnumBinaryEncoder
import ch.softappeal.yass2.core.serialize.string.ClassStringEncoder
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
    val encoderTypes = encoderObjects
        .map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }
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

private fun <P : Property> KSClassDeclaration.properties(createProperty: (property: KSPropertyDeclaration) -> P): List<P> {
    require(!isAbstract()) { "class ${qualifiedName()} must be concrete" }
    val properties = getAllProperties().toList()
        .filterNot { (it.name == "cause" || it.name == "message") && ("kotlin.Throwable" == it.parentDeclaration!!.qualifiedName()) }
        .filter { it.isPublic() } // TODO: see https://github.com/google/ksp/issues/2443
        .map { createProperty(it) }
    val parameters = (primaryConstructor ?: error("class ${qualifiedName()} must hava a primary constructor")).parameters
    val constructorProperties = parameters.map { parameter ->
        properties.firstOrNull { it.name == parameter.name!!.asString() }
            ?: error("primary constructor parameter ${parameter.name!!.asString()} of class ${qualifiedName()} must be a property")
    }
    require(properties.all { it in constructorProperties }) { "class ${qualifiedName()} must not have body properties" }
    return constructorProperties
}

internal fun CodeWriter.generateBinarySerializer(
    encoderObjects: List<KSType>,
    concreteAndEnumClasses: List<KSType>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)

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
                writeNestedLine("// ${List::class.qualifiedName}: $BINARY_LIST_ENCODER_ID")
                var encoderId = BINARY_FIRST_ENCODER_ID
                encoderObjects.forEach { type -> writeNestedLine("${type.qualifiedName}, // ${encoderId++}") }
                enumClasses.forEach { type ->
                    writeNestedLine("${EnumBinaryEncoder::class.qualifiedName}(", "),") {
                        writeNestedLine("${type.qualifiedName}::class, enumValues(), // ${encoderId++}")
                    }
                }
                concreteClasses.forEach { type ->
                    writeNestedLine("${BinaryEncoder::class.qualifiedName}(", "),") {
                        writeNestedLine("${type.qualifiedName}::class, // ${encoderId++}")
                        val properties = (type.declaration as KSClassDeclaration).properties { BinaryProperty(it) }
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

internal fun CodeWriter.generateStringEncoders(
    encoderObjects: List<KSType>,
    concreteAndEnumClasses: List<KSType>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)

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
                encoderId != STRING_STRING_ENCODER_ID &&
                encoderId != STRING_BOOLEAN_ENCODER_ID &&
                encoderId != STRING_LIST_ENCODER_ID
            ) encoderId else STRING_NO_ENCODER_ID
        }"
    }

    writeLine()
    writeNestedLine("public val StringEncoders: List<${StringEncoder::class.qualifiedName}<*>> = listOf(", ")") {
        writeNestedLine("// ${String::class.qualifiedName}: $STRING_STRING_ENCODER_ID")
        writeNestedLine("// ${Boolean::class.qualifiedName}: $STRING_BOOLEAN_ENCODER_ID")
        writeNestedLine("// ${List::class.qualifiedName}: $STRING_LIST_ENCODER_ID")
        var encoderId = STRING_FIRST_ENCODER_ID
        encoderObjects.forEach { type -> writeNestedLine("${type.qualifiedName}, // ${encoderId++}") }
        enumClasses.forEach { type ->
            writeNestedLine("${EnumStringEncoder::class.qualifiedName}(", "),") {
                writeNestedLine("${type.qualifiedName}::class, // ${encoderId++}")
                writeNestedLine("${type.qualifiedName}::valueOf,")
            }
        }
        concreteClasses.forEach { type ->
            writeNestedLine("${ClassStringEncoder::class.qualifiedName}(", "),") {
                writeNestedLine("${type.qualifiedName}::class, // ${encoderId++}")
                val properties = (type.declaration as KSClassDeclaration).properties { StringProperty(it) }
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

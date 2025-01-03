package ch.softappeal.yass2.generate.ksp

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
import ch.softappeal.yass2.serialize.text.TextSerializer
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

private fun checkNotEnum(declaration: KSPropertyDeclaration, classes: List<KSType>) {
    classes.firstOrNull { it.isEnum() }?.let {
        error("enum class ${it.qualifiedName} belongs to enumClasses @${declaration.location}")
    }
}

private fun List<KSType>.checkNotDuplicated(declaration: KSPropertyDeclaration) {
    require(hasNoDuplicates()) { "classes ${duplicates()} are duplicated @${declaration.location}" }
}

private fun KSClassDeclaration.getAllPropertiesNotThrowable() = getAllProperties().toList()
    .filterNot { (it.name == "cause" || it.name == "message") && ("kotlin.Throwable" == it.parentDeclaration!!.qualifiedName()) }
    .sortedBy { it.name }

private fun getBaseClasses(
    encoderClasses: List<KSType>,
    enumClasses: List<KSType>,
    concreteClasses: List<KSType>,
    declaration: KSPropertyDeclaration,
): List<KSType> {
    val encoderTypes =
        encoderClasses.map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }
    val baseClasses = encoderTypes + enumClasses
    (baseClasses + concreteClasses).checkNotDuplicated(declaration)
    checkNotEnum(declaration, encoderTypes + concreteClasses)
    return baseClasses
}

internal fun CodeWriter.generateSerializer(
    binaryEncoderClasses: List<KSType>,
    textEncoderClasses: List<KSType>,
    enumClasses: List<KSType>,
    concreteClasses: List<KSType>,
    declaration: KSPropertyDeclaration,
) {
    abstract class Property(val property: KSPropertyDeclaration)

    class Properties<P : Property>(klass: KSClassDeclaration, createProperty: (property: KSPropertyDeclaration) -> P) {
        val parameter: List<P>
        val body: List<P>
        val all: List<P>

        init {
            require(!klass.isAbstract()) { "class ${klass.qualifiedName()} must be concrete @${declaration.location}" }
            val properties = klass.getAllPropertiesNotThrowable().map { createProperty(it) }
            parameter = buildList {
                val primaryConstructor = klass.primaryConstructor ?: error(
                    "class ${klass.qualifiedName()} must hava a primary constructor @${declaration.location}"
                )
                val parameters = primaryConstructor.parameters
                parameters.forEach { parameter ->
                    require(parameter.isVal || parameter.isVar) {
                        "primary constructor parameter ${parameter.name!!.asString()} of class ${klass.qualifiedName()} must be a property @${declaration.location}"
                    }
                    add(properties.first { it.property.name == parameter.name!!.asString() })
                }
            }
            body = buildList {
                properties
                    .filter { it !in parameter }
                    .forEach { property ->
                        require(property.property.isMutable) {
                            "body property ${property.property.name} of ${property.property.parentDeclaration?.qualifiedName()} must be var @${declaration.location}"
                        }
                        add(property)
                    }
            }
            all = parameter + body
        }
    }

    if (binaryEncoderClasses.isNotEmpty()) {
        val baseClasses = getBaseClasses(binaryEncoderClasses, enumClasses, concreteClasses, declaration)

        class BinaryProperty(property: KSPropertyDeclaration) : Property(property) {
            var kind: PropertyKind
            var encoderId: Int = -1

            init {
                val type = property.type.resolve()
                kind = if (type.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
                val typeNotNullable = type.makeNotNullable()
                val typeNotNullableName = typeNotNullable.qualifiedName
                if (typeNotNullableName == List::class.qualifiedName || typeNotNullableName == "kotlin.collections.MutableList") {
                    encoderId = BINARY_LIST_ENCODER_ID
                } else {
                    val baseClassIndex = baseClasses.indexOfFirst { it == typeNotNullable }
                    if (baseClassIndex >= 0) encoderId = baseClassIndex + BINARY_FIRST_ENCODER_ID else {
                        fun KSType.hasSuperCLass(superClass: KSType) =
                            (this.declaration as KSClassDeclaration).superTypes.map { it.resolve() }.contains(superClass)

                        val concreteClassIndex = concreteClasses.indexOfFirst { it == typeNotNullable }
                        if (concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperCLass(concreteClasses[concreteClassIndex]) }) {
                            encoderId = concreteClassIndex + baseClasses.size + BINARY_FIRST_ENCODER_ID
                        } else kind = PropertyKind.WithId
                    }
                }
            }
        }

        writeLine()
        writeNestedLine("public ${declaration.actual()}fun createBinarySerializer(): ${BinarySerializer::class.qualifiedName} =") {
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
                                val properties = Properties(type.declaration as KSClassDeclaration) { BinaryProperty(it) }
                                writeNestedLine("{ i ->", "},") {
                                    properties.all.forEach { property ->
                                        writeNestedLine("write${property.kind}(${property.encoderId(", ")}i.${property.property.name})")
                                    }
                                }
                                writeNestedLine("{", "}") {
                                    writeNestedLine("val i = ${type.qualifiedName}(", ")") {
                                        properties.parameter.forEach { property ->
                                            writeNestedLine("read${property.kind}(${property.encoderId()}) as ${property.property.type.type()},")
                                        }
                                    }
                                    properties.body.forEach { property ->
                                        writeNestedLine("i.${property.property.name} = read${property.kind}(${property.encoderId()}) as ${property.property.type.type()}")
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
        val baseClasses = getBaseClasses(textEncoderClasses, enumClasses, concreteClasses, declaration)

        class TextProperty(property: KSPropertyDeclaration) : Property(property) {
            var encoderId: Int = -1

            init {
                val type = property.type.resolve()
                val typeNotNullable = type.makeNotNullable()
                val typeNotNullableName = typeNotNullable.qualifiedName
                if (typeNotNullableName == List::class.qualifiedName || typeNotNullableName == "kotlin.collections.MutableList") {
                    encoderId = TEXT_LIST_ENCODER_ID
                } else if (typeNotNullableName == String::class.qualifiedName) {
                    encoderId = TEXT_STRING_ENCODER_ID
                } else {
                    val baseClassIndex = baseClasses.indexOfFirst { it == typeNotNullable }
                    if (baseClassIndex >= 0) encoderId = baseClassIndex + TEXT_FIRST_ENCODER_ID
                }
            }

            fun needsTo() = encoderId >= 0 && encoderId != TEXT_STRING_ENCODER_ID && encoderId != TEXT_LIST_ENCODER_ID
            fun withId() = encoderId < 0
        }

        writeLine()
        writeNestedLine("public ${declaration.actual()}fun createTextSerializer(): ${TextSerializer::class.qualifiedName} =") {
            writeNestedLine("object : ${TextSerializer::class.qualifiedName}() {", "}") {
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
                                val properties = Properties(type.declaration as KSClassDeclaration) { TextProperty(it) }
                                writeNestedLine("{ i ->", "},") {
                                    properties.all.forEach {
                                        writeNestedLine("write${if (it.withId()) "With" else "No"}Id(\"${it.property.name}\",${if (it.withId()) "" else " ${it.encoderId},"} i.${it.property.name})")
                                    }
                                }
                                writeNestedLine("{", "},") {
                                    writeNestedLine("val i = ${type.qualifiedName}(", ")") {
                                        properties.parameter.forEach { property ->
                                            writeNestedLine("getProperty(\"${property.property.name}\") as ${property.property.type.type()},")
                                        }
                                    }
                                    properties.body.forEach { property ->
                                        writeNestedLine("i.${property.property.name} = getProperty(\"${property.property.name}\") as ${property.property.type.type()}")
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

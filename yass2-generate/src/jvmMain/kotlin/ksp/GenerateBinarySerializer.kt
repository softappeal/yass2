package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.PropertyKind
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.Encoder
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.LIST_ENCODER_ID
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

private fun List<KSType>.getBaseEncoderTypes() =
    map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }

internal fun CodeWriter.generateBinarySerializer(
    baseEncoderClasses: List<KSType>,
    enumClasses: List<KSType>,
    concreteClasses: List<KSType>,
    declaration: KSPropertyDeclaration,
) {
    val baseTypes = baseEncoderClasses.getBaseEncoderTypes()
    val baseClasses = baseTypes + enumClasses

    (baseClasses + concreteClasses).checkNotDuplicated(declaration)
    checkNotEnum(declaration, baseTypes + concreteClasses)

    class Property(val property: KSPropertyDeclaration) {
        var kind: PropertyKind
        var encoderId: Int = -1

        init {
            val type = property.type.resolve()
            kind = if (type.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
            val typeNotNullable = type.makeNotNullable()
            val typeNotNullableName = typeNotNullable.qualifiedName
            if (typeNotNullableName == List::class.qualifiedName || typeNotNullableName == "kotlin.collections.MutableList") {
                encoderId = LIST_ENCODER_ID
            } else {
                val baseClassIndex = baseClasses.indexOfFirst { it == typeNotNullable }
                if (baseClassIndex >= 0) {
                    encoderId = baseClassIndex + FIRST_ENCODER_ID
                } else {
                    fun KSType.hasSuperCLass(superClass: KSType) =
                        (this.declaration as KSClassDeclaration).superTypes.map { it.resolve() }.contains(superClass)

                    val concreteClassIndex = concreteClasses.indexOfFirst { it == typeNotNullable }
                    if (concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperCLass(concreteClasses[concreteClassIndex]) }) {
                        encoderId = concreteClassIndex + baseClasses.size + FIRST_ENCODER_ID
                    } else {
                        kind = PropertyKind.WithId
                    }
                }
            }
        }
    }

    class Properties(klass: KSClassDeclaration) {
        val parameter: List<Property>
        val body: List<Property>
        val all: List<Property>

        init {
            require(!klass.isAbstract()) { "class ${klass.qualifiedName()} must be concrete @${declaration.location}" }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
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

    writeLine()
    writeNestedLine("public ${declaration.actual()}fun createBinarySerializer(): ${BinarySerializer::class.qualifiedName} =") {
        writeNestedLine("object : ${BinarySerializer::class.qualifiedName}() {", "}") {
            writeNestedLine("init {", "}") {
                writeNestedLine("initialize(", ")") {
                    baseEncoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
                    enumClasses.forEach { type -> writeNestedLine("${EnumEncoder::class.qualifiedName}(${type.qualifiedName}::class, enumValues()),") }
                    concreteClasses.forEach { type ->
                        fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
                        writeNestedLine("${Encoder::class.qualifiedName}(${type.qualifiedName}::class,", "),") {
                            val properties = Properties(type.declaration as KSClassDeclaration)
                            if (properties.all.isEmpty()) {
                                writeNestedLine("{ _ -> },")
                            } else {
                                writeNestedLine("{ i ->", "},") {
                                    properties.all.forEach { property ->
                                        writeNestedLine("write${property.kind}(${property.encoderId(", ")}i.${property.property.name})")
                                    }
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

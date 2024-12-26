package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.PropertyKind
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.Encoder
import ch.softappeal.yass2.serialize.binary.EnumEncoder
import ch.softappeal.yass2.serialize.binary.FIRST_ENCODER_ID
import ch.softappeal.yass2.serialize.binary.LIST_ENCODER_ID
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

private fun List<KClass<out Encoder<*>>>.getBaseEncoderTypes() =
    map { it.supertypes.first().arguments.first().type!!.classifier as KClass<*> }

public fun CodeWriter.generateBinarySerializer(
    baseEncoderClasses: List<KClass<out Encoder<*>>>,
    enumClasses: List<KClass<*>>,
    concreteClasses: List<KClass<*>>,
) {
    val baseTypes = baseEncoderClasses.getBaseEncoderTypes()
    val baseClasses = baseTypes + enumClasses

    (baseClasses + concreteClasses).checkNotDuplicated()
    checkNotEnum(baseTypes + concreteClasses)
    enumClasses.forEach {
        require(it.isEnum()) { "class ${it.qualifiedName} in enumClasses must be enum" }
    }

    class Property(val property: KProperty1<out Any, *>) {
        var kind: PropertyKind
        var encoderId: Int = -1

        init {
            val type = property.returnType.classifier as KClass<*>
            kind = if (property.returnType.isMarkedNullable) PropertyKind.NoIdOptional else PropertyKind.NoIdRequired
            if (type == List::class) {
                encoderId = LIST_ENCODER_ID
            } else {
                val baseClassIndex = baseClasses.indexOfFirst { it == type }
                if (baseClassIndex >= 0) {
                    encoderId = baseClassIndex + FIRST_ENCODER_ID
                } else {
                    fun KClass<*>.hasSuperCLass(superClass: KClass<*>) = superclasses.contains(superClass)
                    val concreteClassIndex = concreteClasses.indexOfFirst { it == type }
                    if (concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperCLass(concreteClasses[concreteClassIndex]) }) {
                        encoderId = concreteClassIndex + baseClasses.size + FIRST_ENCODER_ID
                    } else {
                        kind = PropertyKind.WithId
                    }
                }
            }
        }
    }

    class Properties(klass: KClass<*>) {
        val parameter: List<Property>
        val body: List<Property>
        val all: List<Property>

        init {
            require(!klass.isAbstract) { "class ${klass.qualifiedName} must be concrete" }
            val properties = klass.getAllPropertiesNotThrowable().map { Property(it) }
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

    writeLine()
    writeNestedLine("public fun createBinarySerializer(): ${BinarySerializer::class.qualifiedName} =") {
        writeNestedLine("object : ${BinarySerializer::class.qualifiedName}() {", "}") {
            writeNestedLine("init {", "}") {
                writeNestedLine("initialize(", ")") {
                    baseEncoderClasses.forEach { type -> writeNestedLine("${type.qualifiedName}(),") }
                    enumClasses.forEach { type -> writeNestedLine("${EnumEncoder::class.qualifiedName}(${type.qualifiedName}::class, enumValues()),") }
                    concreteClasses.forEach { type ->
                        fun Property.encoderId(tail: String = "") = if (kind != PropertyKind.WithId) "$encoderId$tail" else ""
                        writeNestedLine("${Encoder::class.qualifiedName}(${type.qualifiedName}::class,", "),") {
                            val properties = Properties(type)
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

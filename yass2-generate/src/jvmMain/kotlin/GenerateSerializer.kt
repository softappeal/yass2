package ch.softappeal.yass2.generate

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.NotJsPlatform
import ch.softappeal.yass2.core.serialize.Property
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoder
import ch.softappeal.yass2.core.serialize.binary.BinaryProperty
import ch.softappeal.yass2.core.serialize.binary.BinarySerializer
import ch.softappeal.yass2.core.serialize.binary.EnumBinaryEncoder
import ch.softappeal.yass2.core.serialize.string.BaseStringEncoder
import ch.softappeal.yass2.core.serialize.string.ClassStringEncoder
import ch.softappeal.yass2.core.serialize.string.DoubleStringEncoder
import ch.softappeal.yass2.core.serialize.string.EnumStringEncoder
import ch.softappeal.yass2.core.serialize.string.StringEncoder
import ch.softappeal.yass2.core.serialize.string.StringProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters

public fun KClass<*>.isEnum(): Boolean = java.isEnum

private fun checkNotEnum(classes: List<KClass<*>>) {
    classes.firstOrNull { it.isEnum() }?.let { klass -> error("enum class ${klass.qualifiedName} belongs to enumClasses") }
}

private fun List<KClass<*>>.checkNotDuplicated() {
    require(hasNoDuplicates()) { "classes ${duplicates().map { it.qualifiedName }} are duplicated" }
}

private fun KClass<*>.getAllPropertiesNotThrowable() = memberProperties
    .filterNot { (it.name == "cause" || it.name == "message") && isSubclassOf(Throwable::class) }
    .sortedBy { it.name }

public data class Classes(
    val baseClasses: List<KClass<*>>,
    val enumClasses: List<KClass<out Enum<*>>>,
    val concreteClasses: List<KClass<*>>,
)

public fun getClasses(encoderObjects: List<KClass<*>>, concreteAndEnumClasses: List<KClass<*>>): Classes {
    val encoderTypes = encoderObjects.map { it.supertypes.first().arguments.first().type!!.classifier as KClass<*> }
    val enumClasses = concreteAndEnumClasses.filter { it.isEnum() }.map { @Suppress("UNCHECKED_CAST") (it as KClass<Enum<*>>) }
    val concreteClasses = concreteAndEnumClasses.filterNot { it.isEnum() }
    val baseClasses = encoderTypes + enumClasses
    (baseClasses + concreteClasses).checkNotDuplicated()
    checkNotEnum(encoderTypes + concreteClasses)
    return Classes(baseClasses, enumClasses, concreteClasses)
}

/**
 * Concrete classes must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 */
public class Properties<P : Property>(klass: KClass<*>, createProperty: (property: KProperty1<out Any, *>) -> P) {
    public val parameter: List<P>
    public val body: List<P>
    public val all: List<P>

    init {
        require(!klass.isAbstract) { "class ${klass.qualifiedName} must be concrete" }
        val properties = klass.getAllPropertiesNotThrowable().map { createProperty(it) }
        parameter = buildList {
            val primaryConstructor = klass.primaryConstructor ?: error(
                "class ${klass.qualifiedName} must hava a primary constructor"
            )
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
                    require(property.mutable) { "body property ${property.name} of ${klass.qualifiedName} must be var" }
                    add(property)
                }
        }
        all = parameter + body
    }
}

@OptIn(InternalApi::class)
public fun CodeWriter.generateBinarySerializer(
    encoderObjects: List<KClass<out BinaryEncoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)
    writeLine()
    writeNestedLine("object BinarySerializer : ${BinarySerializer::class.qualifiedName}() {", "}") {
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
                        val properties = Properties(type) {
                            BinaryProperty(it, it.returnType, baseClasses, concreteClasses) { superClass ->
                                superclasses.contains(superClass)
                            }
                        }
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

@OptIn(InternalApi::class)
public fun CodeWriter.generateStringEncoders(
    encoderObjects: List<KClass<out BaseStringEncoder<*>>>,
    concreteAndEnumClasses: List<KClass<*>>,
) {
    val (baseClasses, enumClasses, concreteClasses) = getClasses(encoderObjects, concreteAndEnumClasses)
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
                val properties = Properties(type) { StringProperty(it, it.returnType, baseClasses) }
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

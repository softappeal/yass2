package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BINARY_SERIALIZER
import ch.softappeal.yass2.generate.GENERATED_DUMPER
import ch.softappeal.yass2.generate.GENERATED_PROXY
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

internal fun KClass<*>.getAllPropertiesNotThrowable() = memberProperties
    .filter { !isSubclassOf(Throwable::class) || (it.name != "cause" && it.name != "message") }
    .sortedBy { it.name }

internal fun List<KClass<*>>.getBaseEncoderTypes() =
    map { it.supertypes.first().arguments.first().type!!.classifier as KClass<*> }

public fun Path.readAndFixLines(): String = readText().replace("\r\n", "\n")

public enum class Mode { Verify, Write }

private fun generate(sourceDir: Path, mode: Mode, packageName: String, fileName: String, write: CodeWriter.() -> Unit) {
    fun Path.verify(code: String) {
        val existingCode = readAndFixLines()
        check(code == existingCode) {
            "'$this' is\n${">".repeat(120)}\n$existingCode${"<".repeat(120)}\nbut should be\n${
                ">".repeat(120)
            }\n$code${"<".repeat(120)}"
        }
    }

    val builder = StringBuilder()
    builder.appendPackage(packageName)
    CodeWriter(builder).write()
    val program = builder.toString()
    val file = sourceDir.resolve("$fileName.kt")
    when (mode) {
        Mode.Verify -> file.verify(program)
        Mode.Write -> {
            Files.createDirectories(sourceDir)
            file.writeText(program)
        }
    }
}

public fun generateProxy(
    services: Set<KClass<*>>,
    sourceDir: Path, mode: Mode, packageSuffix: String = "",
) {
    require(services.map { it.java.packageName }.toSet().size == 1) { "services $services must be in same package" }
    services.forEach {
        require(it.hasAnnotation<GenerateProxy>()) { "'$it' must be annotated with '${GenerateProxy::class}'" }
    }
    generate(sourceDir, mode, services.first().java.packageName + packageSuffix, GENERATED_PROXY) {
        services
            .sortedBy { it.simpleName }
            .forEach { generateProxy(it) }
    }
}

private fun checkClasses(classes: List<KClass<*>>, enumMessage: String) {
    require(classes.size == classes.toSet().size) { "class must not be duplicated" }
    classes.firstOrNull { it.java.isEnum }?.let { klass -> error("enum class '${klass.qualifiedName}' $enumMessage") }
}

public fun generateBinarySerializer(
    baseEncoderClasses: List<KClass<*>>, enumClasses: List<KClass<*>>,
    treeConcreteClasses: List<KClass<*>>, graphConcreteClasses: List<KClass<*>>, withDumper: Boolean,
    sourceDir: Path, mode: Mode, packageName: String,
) {
    require(enumClasses.size == enumClasses.toSet().size) { "enum classes must not be duplicated" }
    enumClasses.forEach {
        require(it.java.isEnum) { "class '${it.qualifiedName}' in enumClasses must be enum" }
    }
    checkClasses(
        baseEncoderClasses.getBaseEncoderTypes() + treeConcreteClasses + graphConcreteClasses,
        "belongs to 'enumClasses'"
    )
    generate(sourceDir, mode, packageName, GENERATED_BINARY_SERIALIZER) {
        generateBinarySerializer(baseEncoderClasses, enumClasses, treeConcreteClasses, graphConcreteClasses)
    }
    if (!withDumper) return
    generate(sourceDir, mode, packageName, GENERATED_DUMPER) {
        generateDumper(treeConcreteClasses, graphConcreteClasses)
    }
}

public fun generateBinarySerializer(
    property: KProperty<Serializer>,
    sourceDir: Path, mode: Mode, packageSuffix: String = "",
) {
    val annotation = property.findAnnotation<GenerateBinarySerializer>()!!
    generateBinarySerializer(
        annotation.baseEncoderClasses.asList(),
        annotation.enumClasses.asList(),
        annotation.treeConcreteClasses.asList(),
        annotation.graphConcreteClasses.asList(),
        annotation.withDumper,
        sourceDir, mode, property.javaField!!.declaringClass.packageName + packageSuffix
    )
}

public fun generateDumper(
    treeConcreteClasses: List<KClass<*>>, graphConcreteClasses: List<KClass<*>>,
    sourceDir: Path, mode: Mode, packageName: String,
) {
    checkClasses(
        treeConcreteClasses + graphConcreteClasses,
        "must not be specified"
    )
    generate(sourceDir, mode, packageName, GENERATED_DUMPER) {
        generateDumper(treeConcreteClasses, graphConcreteClasses)
    }
}

public fun generateDumper(
    property: KProperty<Dumper>,
    sourceDir: Path, mode: Mode, packageSuffix: String = "",
) {
    val annotation = property.findAnnotation<GenerateDumper>()!!
    generateDumper(
        annotation.treeConcreteClasses.asList(), annotation.graphConcreteClasses.asList(),
        sourceDir, mode, property.javaField!!.declaringClass.packageName + packageSuffix
    )
}

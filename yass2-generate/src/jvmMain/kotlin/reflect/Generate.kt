package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
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

private fun generate(mode: Mode, sourceDir: Path, packageName: String, fileName: String, code: Appendable.() -> Unit) {
    fun Path.verify(code: String) {
        val existingCode = readAndFixLines()
        check(code == existingCode) {
            "file '$this' is\n${">".repeat(120)}\n$existingCode${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$code${
                "<".repeat(120)
            }"
        }
    }

    val builder = StringBuilder()
    builder.appendPackage(packageName)
    builder.code()
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

public fun generateBinarySerializer(
    property: KProperty<Serializer>, sourceDir: Path, mode: Mode, packageSuffix: String = "",
) {
    val annotation = property.findAnnotation<GenerateBinarySerializer>()!!
    generate(mode, sourceDir, property.javaField!!.declaringClass.packageName + packageSuffix, GENERATED_BINARY_SERIALIZER) {
        generateBinarySerializer(
            annotation.baseEncoderClasses.asList(),
            annotation.enumClasses.asList(),
            annotation.treeConcreteClasses.asList(),
            annotation.graphConcreteClasses.asList(),
        )
    }
    if (!annotation.withDumper) return
    generate(mode, sourceDir, property.javaField!!.declaringClass.packageName + packageSuffix, GENERATED_DUMPER) {
        generateDumper(
            annotation.treeConcreteClasses.asList(),
            annotation.graphConcreteClasses.asList(),
        )
    }
}

public fun generateDumper(
    property: KProperty<Dumper>, sourceDir: Path, mode: Mode, packageSuffix: String = "",
) {
    val annotation = property.findAnnotation<GenerateDumper>()!!
    generate(mode, sourceDir, property.javaField!!.declaringClass.packageName + packageSuffix, GENERATED_DUMPER) {
        generateDumper(
            annotation.treeConcreteClasses.asList(),
            annotation.graphConcreteClasses.asList(),
        )
    }
}

public fun generateProxy(
    services: Set<KClass<*>>, sourceDir: Path, mode: Mode, packageSuffix: String = "",
) {
    check(services.map { it.java.packageName }.toSet().size == 1)
    generate(mode, sourceDir, services.first().java.packageName + packageSuffix, GENERATED_PROXY) {
        services
            .sortedBy { it.simpleName }
            .onEach {
                it.java.packageName
                check(it.hasAnnotation<GenerateProxy>()) { "'$it' is not annotated with '${GenerateProxy::class}'" }
            }
            .forEach { generateProxy(it) }
    }
}

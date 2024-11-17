package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.generate.readAndFixLines
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals

internal fun KClass<*>.isEnum() = java.isEnum

internal fun checkNotEnum(classes: List<KClass<*>>, message: String) {
    classes.firstOrNull { it.isEnum() }?.let { klass -> error("enum class ${klass.qualifiedName} $message") }
}

internal fun List<KClass<*>>.checkNotDuplicated() {
    require(hasNoDuplicates()) { "classes ${duplicates().map { it.qualifiedName }} are duplicated" }
}

internal fun KClass<*>.getAllPropertiesNotThrowable() = memberProperties
    .filterNot { (it.name == "cause" || it.name == "message") && isSubclassOf(Throwable::class) }
    .sortedBy { it.name }

public fun CodeWriter.generateBinarySerializer(property: KProperty<Serializer>) {
    val annotation = property.findAnnotation<GenerateBinarySerializer>()!!
    generateBinarySerializer(
        annotation.baseEncoderClasses.asList(),
        annotation.enumClasses.asList(),
        annotation.treeConcreteClasses.asList(),
        annotation.graphConcreteClasses.asList(),
    )
    if (annotation.withDumper) generateDumper(annotation.treeConcreteClasses.asList(), annotation.graphConcreteClasses.asList())
}

public fun CodeWriter.generateDumper(property: KProperty<Dumper>) {
    val annotation = property.findAnnotation<GenerateDumper>()!!
    generateDumper(annotation.treeConcreteClasses.asList(), annotation.graphConcreteClasses.asList())
}

public enum class Mode { Verify, Write }

public fun generate(sourceDir: String, packageName: String, mode: Mode, write: CodeWriter.() -> Unit) {
    val builder = StringBuilder()
    builder.appendPackage(packageName)
    CodeWriter(builder).write()
    val program = builder.toString()
    val sourcePath = kotlin.io.path.Path(sourceDir)
    val file = sourcePath.resolve("$GENERATED_BY_YASS.kt")
    when (mode) {
        Mode.Verify -> {
            val existingCode = file.readAndFixLines()
            assertEquals(existingCode, program)
        }
        Mode.Write -> {
            Files.createDirectories(sourcePath)
            file.writeText(program)
        }
    }
}

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.readAndFixLines
import ch.softappeal.yass2.serialize.GenerateSerializer
import ch.softappeal.yass2.serialize.Serializer
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.test.assertEquals

internal fun KClass<*>.isEnum() = java.isEnum

public fun CodeWriter.generateBinarySerializer(property: KProperty<Serializer>) {
    val annotation = property.findAnnotation<GenerateSerializer>()!!
    val concreteClasses = annotation.concreteClasses.asList()
    generateSerializer(
        annotation.binaryEncoderClasses.asList(),
        annotation.textEncoderClasses.asList(),
        concreteClasses.filter { it.isEnum() }.map { @Suppress("UNCHECKED_CAST") (it as KClass<Enum<*>>) },
        concreteClasses.filterNot { it.isEnum() },
    )
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

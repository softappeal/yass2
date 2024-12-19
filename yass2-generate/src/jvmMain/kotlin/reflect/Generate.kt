package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.readAndFixLines
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.test.assertEquals

public fun CodeWriter.generateBinarySerializer(property: KProperty<Serializer>) {
    val annotation = property.findAnnotation<GenerateBinarySerializer>()!!
    generateBinarySerializer(
        annotation.baseEncoderClasses.asList(),
        annotation.enumClasses.asList(),
        annotation.treeConcreteClasses.asList(),
        annotation.graphConcreteClasses.asList(),
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

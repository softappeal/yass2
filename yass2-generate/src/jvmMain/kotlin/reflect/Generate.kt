package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.readAndFixLines
import ch.softappeal.yass2.serialize.binary.BaseEncoder
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals

public enum class Mode { Verify, Write }

public fun generate(sourceDir: String, packageName: String, mode: Mode, write: CodeWriter.() -> Unit) {
    val builder = StringBuilder()
    builder.appendPackage(packageName)
    CodeWriter(builder).write()
    val program = builder.toString()
    val sourcePath = kotlin.io.path.Path(sourceDir)
    val file = sourcePath.resolve(GENERATED_BY_YASS)
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

internal fun KClass<*>.getAllPropertiesNotThrowable() = memberProperties
    .filter { !isSubclassOf(Throwable::class) || (it.name != "cause" && it.name != "message") }
    .sortedBy { it.name }

internal fun checkNotEnum(classes: List<KClass<*>>, message: String) {
    classes.firstOrNull { it.java.isEnum }?.let { klass -> error("enum class '${klass.qualifiedName}' $message") }
}

public fun CodeWriter.generateBinarySerializerAndDumper(
    baseEncoders: List<BaseEncoder<out Any>>,
    treeConcreteClasses: List<KClass<*>>,
    graphConcreteClasses: List<KClass<*>> = emptyList(),
) {
    generateBinarySerializer(baseEncoders, treeConcreteClasses, graphConcreteClasses)
    generateDumper(treeConcreteClasses, graphConcreteClasses)
}

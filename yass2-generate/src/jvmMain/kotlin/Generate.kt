package ch.softappeal.yass2.generate

import ch.softappeal.yass2.serialize.binary.BaseEncoder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals

public fun Path.readAndFixLines(): String = readText().replace("\r\n", "\n")

public class CodeWriter(private val appendable: Appendable, private val depth: Int = 0) {
    public fun writeLine() {
        appendable.appendLine()
    }

    public fun write(s: String) {
        appendable.append(s)
    }

    private fun nested(write: CodeWriter.() -> Unit) {
        CodeWriter(appendable, depth + 1).write()
    }

    public fun writeLine(s: String, write: CodeWriter.() -> Unit) {
        write(s)
        writeLine()
        nested(write)
    }

    public fun writeNested(s: String) {
        appendable.append("    ".repeat(depth))
        write(s)
    }

    public fun writeNestedLine(s: String) {
        writeNested(s)
        writeLine()
    }

    public fun writeNestedLine(s: String, write: CodeWriter.() -> Unit) {
        writeNestedLine(s)
        nested(write)
    }
}

public enum class Mode { Verify, Write }

public const val GENERATED_BY_YASS: String = "GeneratedByYass.kt"

public fun generate(sourceDir: String, packageName: String, mode: Mode, write: CodeWriter.() -> Unit) {
    fun Appendable.appendPackage(packageName: String) {
        appendLine("""
            @file:Suppress(
                "UNCHECKED_CAST",
                "USELESS_CAST",
                "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
                "unused",
                "RemoveRedundantQualifierName",
                "SpellCheckingInspection",
                "RedundantVisibilityModifier",
                "RedundantNullableReturnType",
                "KotlinRedundantDiagnosticSuppress",
                "RedundantSuppression",
            )
        
            package $packageName
        """.trimIndent())
    }

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

internal const val CSY = "ch.softappeal.yass2"

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

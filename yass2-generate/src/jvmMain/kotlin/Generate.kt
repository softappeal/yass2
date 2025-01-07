package ch.softappeal.yass2.generate

import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.test.assertEquals

internal const val CSY = "ch.softappeal.yass2"

internal fun Appendable.appendPackage(packageName: String) {
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
            "UNUSED_ANONYMOUS_PARAMETER",
        )
    
        package $packageName
    """.trimIndent())
}

public fun <T> List<T>.hasNoDuplicates(): Boolean = size == toSet().size

public fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

// fixes "kotlin.Exception /* = java.lang.Exception */"
internal fun KType.convert() = if (classifier is KClass<*> && classifier == Exception::class) "kotlin.Exception" else toString()

public class CodeWriter(private val appendable: Appendable, private val indent: String = "") {
    public fun writeLine() {
        appendable.appendLine()
    }

    public fun write(s: String) {
        appendable.append(s)
    }

    private fun nested(write: CodeWriter.() -> Unit) {
        CodeWriter(appendable, "$indent    ").write()
    }

    public fun writeLine(s: String, write: CodeWriter.() -> Unit) {
        write(s)
        writeLine()
        nested(write)
    }

    public fun writeNested(s: String) {
        write(indent)
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

    public fun writeNestedLine(start: String, end: String, write: CodeWriter.() -> Unit) {
        writeNestedLine(start, write)
        writeNestedLine(end)
    }
}

public fun CodeWriter.generateProxies(services: List<KClass<*>>) {
    services.forEach(::generateProxy)
}

public const val GENERATED_BY_YASS: String = "GeneratedByYass.kt"

public enum class Mode { Verify, Write }

public fun generate(sourceDir: String, packageName: String, mode: Mode, write: CodeWriter.() -> Unit) {
    val builder = StringBuilder()
    builder.appendPackage(packageName)
    CodeWriter(builder).write()
    val program = builder.toString()
    val sourcePath = Path(sourceDir)
    val file = sourcePath.resolve(GENERATED_BY_YASS)
    when (mode) {
        Mode.Verify -> {
            val existingCode = file.readText().replace("\r\n", "\n")
            assertEquals(existingCode, program)
        }
        Mode.Write -> {
            Files.createDirectories(sourcePath)
            file.writeText(program)
        }
    }
}

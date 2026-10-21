package ch.softappeal.yass2.generate

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal fun <T> List<T>.hasNoDuplicates() = size == toSet().size

internal fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

internal inline fun <T> Iterable<T>.forEach(separator: () -> Unit, action: (T) -> Unit) {
    val iterator = iterator()
    if (!iterator.hasNext()) return
    action(iterator.next())
    while (iterator.hasNext()) {
        separator()
        action(iterator.next())
    }
}

internal const val RemotePackage = "ch.softappeal.yass2.remote"

internal fun Appendable.appendPackage(packageName: String) {
    append(
        """
            @file:Suppress(
                "unused",
                "UNCHECKED_CAST",
                "USELESS_CAST",
                "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
                "RemoveRedundantQualifierName",
                "SpellCheckingInspection",
                "RedundantVisibilityModifier",
                "REDUNDANT_VISIBILITY_MODIFIER",
                "RedundantSuppression",
                "UNUSED_ANONYMOUS_PARAMETER",
                "KotlinRedundantDiagnosticSuppress",
                "RedundantUpperBound",
            )

            package $packageName

        """.trimIndent()
    )
}

internal class CodeWriter private constructor(private val appendable: Appendable, private val indent: String) {
    constructor(appendable: Appendable) : this(appendable, "")

    internal fun writeLine() {
        appendable.append('\n')
    }

    internal fun write(s: String) {
        appendable.append(s)
    }

    internal fun nested(write: CodeWriter.() -> Unit) {
        CodeWriter(appendable, "$indent    ").write()
    }

    internal fun writeLine(s: String, write: CodeWriter.() -> Unit) {
        write(s)
        writeLine()
        nested(write)
    }

    internal fun writeNested(s: String) {
        write(indent)
        write(s)
    }

    internal fun writeNestedLine(s: String) {
        writeNested(s)
        writeLine()
    }

    internal fun writeNestedLine(s: String, write: CodeWriter.() -> Unit) {
        writeNestedLine(s)
        nested(write)
    }

    internal fun writeNestedLine(start: String, end: String, write: CodeWriter.() -> Unit) {
        writeNestedLine(start, write)
        writeNestedLine(end)
    }
}

internal fun CodeWriter.writeFun(signature: String, body: CodeWriter.() -> Unit) {
    writeLine()
    writeNestedLine("public fun $signature =")
    nested { body() }
}

internal fun CodeWriter.writeFun(head: String, parameter: String, tail: String, body: CodeWriter.() -> Unit) {
    writeLine()
    writeNestedLine("public fun$head(", "): $tail =") {
        writeNestedLine("$parameter,")
    }
    nested { body() }
}

/** Name of the generated file (without `.kt` extension). */
public const val GENERATED_BY_YASS: String = "GeneratedByYass"

internal fun String.fixLines() = replace("\r\n", "\n")

internal fun writeGeneratedFile(generatedDir: Path, generatedCode: String) {
    val generatedFile = generatedDir.resolve("$GENERATED_BY_YASS.kt")
    if (generatedFile.notExists() || generatedCode != generatedFile.readText().fixLines()) {
        generatedFile.writeText(generatedCode)
        error("Generated file '${generatedFile.absolutePathString()}' has been changed. COMMIT IT TO REPOSITORY!")
    }
}

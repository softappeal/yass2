package ch.softappeal.yass2.generate

import java.nio.file.Path
import kotlin.io.path.readText

public fun Path.readAndFixLines(): String = readText().replace("\r\n", "\n")

public const val GENERATED_BY_YASS: String = "GeneratedByYass"

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
        )
    
        package $packageName
    """.trimIndent())
}

internal enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

public fun <T> List<T>.hasNoDuplicates() = size == toSet().size

public fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

internal fun <T> List<T>.sortMethods(methodName: T.() -> String, interfaceName: () -> String, location: String = "") =
    sortedBy { it.methodName() }
        .apply {
            val methodNames = map { it.methodName() }
            require(methodNames.hasNoDuplicates()) {
                "interface ${interfaceName()} has overloaded methods ${methodNames.duplicates()}$location" // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
            }
        }

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
        write("    ".repeat(depth))
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

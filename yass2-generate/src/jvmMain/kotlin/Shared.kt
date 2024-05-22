package ch.softappeal.yass2.generate

public const val GENERATED_PROXY: String = "GeneratedProxy"
public const val GENERATED_BINARY_SERIALIZER: String = "GeneratedBinarySerializer"
public const val GENERATED_DUMPER: String = "GeneratedDumper"

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

public class CodeWriter(private val appendable: Appendable, private val depth: Int = 0) {
    public fun writeLine() {
        appendable.appendLine()
    }

    public fun write(s: String) {
        appendable.append(s)
    }

    public fun writeLine(s: String) {
        write(s)
        writeLine()
    }

    private fun nested(write: CodeWriter.() -> Unit) {
        CodeWriter(appendable, depth + 1).write()
    }

    public fun writeLine(s: String, write: CodeWriter.() -> Unit) {
        writeLine(s)
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

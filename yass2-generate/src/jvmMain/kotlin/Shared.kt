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

internal fun Appendable.append(level: Int, s: String): Appendable {
    append("    ".repeat(level)).append(s)
    return this
}

internal fun Appendable.appendLine(level: Int, s: String) {
    append(level, s).appendLine()
}

internal enum class PropertyKind { WithId, NoIdRequired, NoIdOptional }

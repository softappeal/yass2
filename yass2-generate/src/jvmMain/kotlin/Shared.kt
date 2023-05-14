package ch.softappeal.yass2.generate

import java.nio.file.*
import kotlin.io.path.*

internal fun Appendable.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal const val CSY = "ch.softappeal.yass2"

internal fun Appendable.writeHeader(packageName: String) {
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

public const val GENERATED_PROXY: String = "GeneratedProxy"
public const val GENERATED_BINARY_SERIALIZER: String = "GeneratedBinarySerializer"
public const val GENERATED_DUMPER_PROPERTIES: String = "GeneratedDumperProperties"

public fun Path.readAndFixLines(): String = readText().replace("\r\n", "\n")

public fun Path.verify(code: String) {
    val existingCode = readAndFixLines()
    check(code == existingCode) {
        "file '$this' is\n${">".repeat(120)}\n$existingCode${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$code${"<".repeat(120)}"
    }
}

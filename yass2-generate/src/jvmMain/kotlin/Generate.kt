@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.core.InternalApi
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal fun <T> List<T>.hasNoDuplicates() = size == toSet().size

internal fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

internal const val CSY = "ch.softappeal.yass2"

internal fun KType.toType() = toString() // TODO: see file 'KTypeToTypeTest.kt'
    .replace("kotlin.Exception /* = java.lang.Exception */", "kotlin.Exception")

public class CodeWriter private constructor(private val appendable: Appendable, private val indent: String) {
    @InternalApi public constructor(appendable: Appendable) : this(appendable, "")

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

public const val GENERATED_BY_YASS: String = "GeneratedByYass"

public fun generateFile(generatedDir: String, packageName: String, write: CodeWriter.() -> Unit) {
    val builder = StringBuilder()
    builder.append(
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
            )

            package $packageName

        """.trimIndent()
    )
    CodeWriter(builder).write()
    val generatedFile = Path(generatedDir).resolve("$GENERATED_BY_YASS.kt")
    Files.createDirectories(generatedFile.parent)
    generatedFile.writeText(builder.toString())
}

public fun CodeWriter.generateProxies(services: List<KClass<*>>) {
    services.forEach(::generateProxy)
}

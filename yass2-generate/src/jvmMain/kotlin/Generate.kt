package ch.softappeal.yass2.generate

import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KType

public const val CSY: String = "ch.softappeal.yass2"

private fun Appendable.appendPackage(packageName: String) {
    append(
        """
            @file:Suppress(
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
}

internal fun <T> List<T>.hasNoDuplicates() = size == toSet().size

internal fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

public fun KType.toType(): String = toString() // TODO: see file 'KTypeToTypeTest.kt'
    .replace("kotlin.Exception /* = java.lang.Exception */", "kotlin.Exception")

public class CodeWriter private constructor(private val appendable: Appendable, private val indent: String) {
    public constructor(appendable: Appendable) : this(appendable, "")

    public fun writeLine() {
        appendable.append('\n')
    }

    public fun write(s: String) {
        appendable.append(s)
    }

    public fun nested(write: CodeWriter.() -> Unit) {
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

public fun generateFile(filePath: String, packageName: String, write: CodeWriter.() -> Unit) {
    val builder = StringBuilder()
    builder.appendPackage(packageName)
    CodeWriter(builder).write()
    val file = Path(filePath)
    Files.createDirectories(file.parent)
    file.writeText(builder.toString())
}

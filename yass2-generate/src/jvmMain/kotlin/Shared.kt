package ch.softappeal.yass2.generate

import ch.softappeal.yass2.core.InternalApi

internal fun <T> List<T>.hasNoDuplicates() = size == toSet().size

internal fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

internal fun <M> List<M>.sortMethods(interfaceName: () -> String, methodName: M.() -> String, isSuspend: M.() -> Boolean) =
    onEach {
        require(it.isSuspend()) { "method ${interfaceName()}.${it.methodName()} must be suspend" }
    }
        .sortedBy { it.methodName() }
        .apply {
            val methodNames = map { it.methodName() }
            require(methodNames.hasNoDuplicates()) {
                "interface ${interfaceName()} has overloaded methods ${methodNames.duplicates()}" // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
            }
        }

internal const val CSY = "ch.softappeal.yass2"

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
            )

            package $packageName

        """.trimIndent()
    )
}

public class CodeWriter private constructor(private val appendable: Appendable, private val indent: String) {
    @InternalApi
    public constructor(appendable: Appendable) : this(appendable, "")

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

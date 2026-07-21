@file:OptIn(TestingYassApi::class)

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.core.TestingYassApi
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KType

public class CodeWriter private constructor(private val appendable: Appendable, private val indent: String) {
    /** @suppress */
    @TestingYassApi public constructor(appendable: Appendable) : this(appendable, "")

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

internal fun <T> List<T>.hasNoDuplicates() = size == toSet().size

internal fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

internal const val CSY = "ch.softappeal.yass2"

/** Name of the generated file. */
public const val GENERATED_BY_YASS: String = "GeneratedByYass.kt"

private val RemoveComment = Regex(""" *?/\*.*?\*/""") // TODO: see file 'KTypeToTypeTest.kt'

/** @suppress */
@TestingYassApi public fun String.removeComment(): String = replace(RemoveComment, "")

internal fun KType.toType() = toString().removeComment()

internal fun CodeWriter.writeFun(signature: String, body: CodeWriter.() -> Unit) {
    writeLine()
    writeNestedLine("public fun$signature =")
    nested { body() }
}

private fun Appendable.appendPackage(packageName: String) {
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

private fun String.fixLines() = replace("\r\n", "\n")

/**
 * Generates a file with name [GENERATED_BY_YASS] at [generatedDir] with the package of the receiver.
 *
 * Usage: Add a test in the package of the generated file.
 * ```
 * class GenerateTest {
 *     @Test
 *     fun generate() {
 *         generateFile(...) {
 *             ...
 *         }
 *     }
 * }
 * ```
 */
public fun Any.generateFile(generatedDir: String, generate: CodeWriter.() -> Unit) {
    val generatedCode = buildString {
        appendPackage(this@generateFile::class.java.`package`.name)
        CodeWriter(this).generate()
    }
    val generatedFile = Path(generatedDir).resolve(GENERATED_BY_YASS)
    if (generatedFile.notExists() || generatedCode != generatedFile.readText().fixLines()) {
        generatedFile.writeText(generatedCode)
        error("Generated file '${generatedFile.absolutePathString()}' has been changed. COMMIT IT TO REPOSITORY!")
    }
}

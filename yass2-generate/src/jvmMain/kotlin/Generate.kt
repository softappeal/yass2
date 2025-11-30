@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.core.InternalApi
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.test.assertEquals

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

public const val GENERATED_BY_YASS: String = "GeneratedByYass.kt"

public enum class GenerateMode {
    /** Create/Overwrite the generated file. */
    Update,

    /** Fail if generated the file differs from existing. */
    Check,
}

/**
 * Generates a file with name [GENERATED_BY_YASS]  at [generatedDir] with the package of the receiver.
 *
 * Usage: Add a test in the package of the generated file.
 * ```
 * class GenerateTest {
 *     @Test
 *     fun generate() {
 *         generateFile("src/commonMain/kotlin", GenerateMode.Check) { ... }
 *     }
 * }
 * ```
 */
public fun Any.generateFile(generatedDir: String, mode: GenerateMode = GenerateMode.Update, write: CodeWriter.() -> Unit) {
    val generatedCode = buildString {
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

                package ${this@generateFile::class.java.`package`.name}

            """.trimIndent()
        )
        CodeWriter(this).write()
    }
    val generatedFile = Path(generatedDir).resolve(GENERATED_BY_YASS)
    when (mode) {
        GenerateMode.Update -> {
            Files.createDirectories(generatedFile.parent)
            generatedFile.writeText(generatedCode)
        }
        GenerateMode.Check -> {
            val existingCode = generatedFile.readText().replace("\r\n", "\n")
            assertEquals(
                generatedCode, existingCode, // enables convenient diff in IntelliJ IDEA
                "outdated generated file '${generatedFile.absolutePathString()}' (use generateFile with GenerateMode.Update)",
            )
        }
    }
}

public fun CodeWriter.generateProxies(services: List<KClass<*>>) {
    services.forEach(::generateProxy)
}

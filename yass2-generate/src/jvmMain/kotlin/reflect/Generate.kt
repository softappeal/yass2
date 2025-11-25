@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.test.assertEquals

internal fun KType.toType() = toString() // TODO: see file 'KTypeToTypeTest.kt'
    .replace("kotlin.Exception /* = java.lang.Exception */", "kotlin.Exception")

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
        appendPackage(this@generateFile::class.java.`package`.name)
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

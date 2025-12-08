@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation

internal fun KType.toType() = toString() // TODO: see file 'KTypeToTypeTest.kt'
    .replace("kotlin.Exception /* = java.lang.Exception */", "kotlin.Exception")

public enum class GenerateMode {
    /** Create/Overwrite the generated file. */
    Update,

    /** Fail if generated the file differs from existing. */
    Check,
}

public fun Any.generateCode(write: CodeWriter.() -> Unit): String = buildString {
    appendPackage(this@generateCode::class.java.`package`.name)
    CodeWriter(this).write()
}

/**
 * Generates a file with name [GENERATED_BY_YASS] at [generatedDir] with the package of the receiver.
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
    val generatedCode = generateCode(write)
    val generatedFile = Path(generatedDir).resolve("$GENERATED_BY_YASS.kt")
    when (mode) {
        GenerateMode.Update -> {
            Files.createDirectories(generatedFile.parent)
            generatedFile.writeText(generatedCode)
        }
        GenerateMode.Check -> {
            val existingCode = generatedFile.readText().replace("\r\n", "\n")
            check(generatedCode == existingCode) {
                "outdated generated file '${generatedFile.absolutePathString()}' (use generateFile with GenerateMode.Update)"
            }
        }
    }
}

/** [annotatedElement] must be annotated with [Proxies]. */
public fun CodeWriter.generateProxies(annotatedElement: KAnnotatedElement) {
    val services = annotatedElement.findAnnotation<Proxies>()!!.value.toList()
    services.forEach(::generateProxy)
}

/** [annotatedElement] must be annotated with [BinaryEncoderObjects] and [ConcreteAndEnumClasses]. */
public fun CodeWriter.generateBinarySerializer(annotatedElement: KAnnotatedElement) {
    generateBinarySerializer(
        annotatedElement.findAnnotation<BinaryEncoderObjects>()!!.value.toList(),
        annotatedElement.findAnnotation<ConcreteAndEnumClasses>()!!.value.toList(),
    )
}

/** [annotatedElement] must be annotated with [StringEncoderObjects] and [ConcreteAndEnumClasses]. */
public fun CodeWriter.generateStringEncoders(annotatedElement: KAnnotatedElement) {
    generateStringEncoders(
        annotatedElement.findAnnotation<StringEncoderObjects>()!!.value.toList(),
        annotatedElement.findAnnotation<ConcreteAndEnumClasses>()!!.value.toList(),
    )
}

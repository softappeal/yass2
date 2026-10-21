package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.writeGeneratedFile
import ch.softappeal.yass2.remote.Proxies
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.serialize.string.StringEncoderObjects
import kotlin.io.path.Path
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation

private val RemoveComment = Regex(""" *?/\*.*?\*/""") // TODO: see file 'KTypeToTypeTest.kt'

internal fun String.removeComment(): String = replace(RemoveComment, "")

internal fun KType.toType() = toString().removeComment()

private fun Any.generateCode(annotatedElement: KAnnotatedElement) = buildString {
    appendPackage(this@generateCode::class.java.`package`.name)
    with(CodeWriter(this)) {
        val services = annotatedElement.findAnnotation<Proxies>()
        services?.value?.forEach(::generateProxy)

        val concreteAndEnumClasses = annotatedElement.findAnnotation<ConcreteAndEnumClasses>()
        val binaryEncoderObjects = annotatedElement.findAnnotation<BinaryEncoderObjects>()
        val stringEncoderObjects = annotatedElement.findAnnotation<StringEncoderObjects>()
        if (concreteAndEnumClasses == null) {
            require(binaryEncoderObjects == null && stringEncoderObjects == null) {
                "missing annotation '${ConcreteAndEnumClasses::class.qualifiedName}'"
            }
        } else {
            require(binaryEncoderObjects != null || stringEncoderObjects != null) {
                "missing annotations '${BinaryEncoderObjects::class.qualifiedName}' or '${StringEncoderObjects::class.qualifiedName}'"
            }
            binaryEncoderObjects?.let { generateBinarySerializer(it.value.toList(), concreteAndEnumClasses.value.toList()) }
            stringEncoderObjects?.let { generateStringEncoders(it.value.toList(), concreteAndEnumClasses.value.toList()) }
        }
    }
}

/**
 * Generates a file with name [GENERATED_BY_YASS] at [generatedDir] with the package of the receiver.
 *
 * Usage: Add a test in the package of the generated file.
 * ```
 * class GenerateTest {
 *     @Test
 *     fun generate() {
 *         generateFile(...)
 *     }
 * }
 * ```
 */
public fun Any.generateFile(generatedDir: String, annotatedElement: KAnnotatedElement) {
    writeGeneratedFile(Path(generatedDir), generateCode(annotatedElement))
}

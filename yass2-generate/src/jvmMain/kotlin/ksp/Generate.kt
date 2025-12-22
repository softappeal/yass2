@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.forEachSeparator
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.fixLines
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass

internal fun KSDeclaration.qualifiedName() = qualifiedName!!.asString()
internal val KSDeclaration.name get() = simpleName.asString()
internal val KSType.qualifiedName get() = declaration.qualifiedName()

internal fun KSTypeReference.toType(): String {
    fun Appendable.appendGenerics() {
        val typeArguments = element!!.typeArguments
        if (typeArguments.isEmpty()) return
        append('<')
        typeArguments.forEachSeparator({ append(", ") }) { typeArgument ->
            val variance = typeArgument.variance
            append(variance.label)
            when (variance) {
                Variance.STAR -> return@forEachSeparator
                Variance.INVARIANT -> {}
                Variance.COVARIANT, Variance.CONTRAVARIANT -> append(' ')
            }
            append(typeArgument.type!!.toType())
        }
        append('>')
    }
    return buildString {
        val type = resolve()
        if (type.declaration is KSTypeParameter) append(type.declaration.name) else append(type.qualifiedName).appendGenerics()
        if (type.isMarkedNullable) append('?')
    }
}

@Suppress("UNCHECKED_CAST")
private fun KSAnnotation.value() = arguments.first { it.name!!.asString() == "value" }.value as List<KSType>

public enum class GenerateMode {
    /**
     * Generate the code in the build directory. That's the normal KSP way.
     * ```
     * TODO: KSP works only for platform code (see https://github.com/google/ksp/issues/2233).
     *       Common/intermediate (= none-platform) code cannot reference generated code in the compilation of platform code.
     *       Generated code is treated as platform code (you'll have to use expect/actual).
     * ```
     * see [WithExpectAndActual]
     */
    InBuildDir,

    /** Generate the code in the code repository. */
    InRepository,

    /** Generate the `expect` code in the code repository and the `actual` code in the build directory. */
    WithExpectAndActual,
}

/**
 * KSP argument for setting the [GenerateMode].
 * Uses [GenerateMode.InBuildDir] if not specified.
 */
public const val GENERATE_MODE: String = "yass.GenerateMode"

internal fun CodeWriter.writeFun(
    signature: String,
    expectWriter: CodeWriter?, expectLine: Boolean = true,
    body: CodeWriter.() -> Unit,
) {
    expectWriter?.apply {
        if (expectLine) writeLine()
        writeNestedLine("public expect fun$signature")
    }
    writeLine()
    writeNestedLine("public ${if (expectWriter == null) "" else "actual "}fun$signature =")
    nested { body() }
}

private fun processPackage(name: String, declarations: Set<KSDeclaration>, environment: SymbolProcessorEnvironment) {
    val generateMode = GenerateMode.valueOf(environment.options[GENERATE_MODE] ?: GenerateMode.InBuildDir.name)

    fun annotationOrNull(annotation: KClass<*>) = declarations
        .mapNotNull { declaration ->
            declaration.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName() == annotation.qualifiedName }
        }
        .also { annotations ->
            require(annotations.size <= 1) { "there can be at most one annotation '${annotation.qualifiedName}' in package '$name'" }
        }
        .firstOrNull()

    val code = StringBuilder()
    code.appendPackage(name)
    val writer = CodeWriter(code)

    val expectCode = StringBuilder()
    val expectWriter = if (generateMode == GenerateMode.WithExpectAndActual) CodeWriter(expectCode) else null

    annotationOrNull(Proxies::class)?.let {
        it.value().forEach { type -> writer.generateProxy(type.declaration as KSClassDeclaration, expectWriter) }
    }

    val concreteAndEnumClasses = annotationOrNull(ConcreteAndEnumClasses::class)
    val binaryEncoderObjects = annotationOrNull(BinaryEncoderObjects::class)
    val stringEncoderObjects = annotationOrNull(StringEncoderObjects::class)
    if (concreteAndEnumClasses == null) {
        require(binaryEncoderObjects == null && stringEncoderObjects == null) {
            "missing annotation '${ConcreteAndEnumClasses::class.qualifiedName}' in package '$name'"
        }
    } else {
        require(binaryEncoderObjects != null || stringEncoderObjects != null) {
            "missing annotations '${BinaryEncoderObjects::class.qualifiedName}' or '${StringEncoderObjects::class.qualifiedName}' in package '$name'"
        }
        binaryEncoderObjects?.let { writer.generateBinarySerializer(it.value(), concreteAndEnumClasses.value(), expectWriter) }
        stringEncoderObjects?.let { writer.generateStringEncoders(it.value(), concreteAndEnumClasses.value(), expectWriter) }
    }

    if (generateMode == GenerateMode.InBuildDir || generateMode == GenerateMode.WithExpectAndActual) {
        environment.codeGenerator.createNewFile(Dependencies.ALL_FILES, name, GENERATED_BY_YASS).writer().use { it.append(code) }
    }
    if (generateMode == GenerateMode.InRepository || generateMode == GenerateMode.WithExpectAndActual) {
        val repositoryFile = Path((declarations.first().location as FileLocation).filePath).parent.resolve("$GENERATED_BY_YASS.kt")
        val repositoryCode = if (generateMode == GenerateMode.InRepository) code.toString() else buildString {
            appendPackage(name)
            append(expectCode)
        }
        if (repositoryFile.notExists()) repositoryFile.writeText(repositoryCode) else {
            val existingCode = repositoryFile.readText().fixLines()
            check(repositoryCode == existingCode) {
                "outdated generated file '${repositoryFile.absolutePathString()}' (delete file to regenerate it)"
            }
        }
    }
}

/** @suppress */
@InternalApi public class Yass2Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = object : SymbolProcessor {
        override fun process(resolver: Resolver): List<KSAnnotated> {
            listOf(
                Proxies::class,
                ConcreteAndEnumClasses::class,
                BinaryEncoderObjects::class,
                StringEncoderObjects::class,
            )
                .flatMap { annotation -> resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!) }
                .map { annotated -> annotated as KSDeclaration }
                .groupBy { declaration -> declaration.packageName.asString() }
                .forEach { (name, declarations) -> processPackage(name, declarations.toSet(), environment) }
            return emptyList()
        }
    }
}

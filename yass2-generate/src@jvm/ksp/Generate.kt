package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.forEach
import ch.softappeal.yass2.generate.writeGeneratedFile
import ch.softappeal.yass2.remote.Proxies
import ch.softappeal.yass2.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.serialize.binary.BinaryEncoders
import ch.softappeal.yass2.serialize.string.StringEncoders
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
import kotlin.reflect.KClass

internal fun KSDeclaration.qualifiedName() = qualifiedName!!.asString()
internal val KSDeclaration.name get() = simpleName.asString()
internal val KSType.qualifiedName get() = declaration.qualifiedName()

internal fun KSTypeReference?.toType(): String {
    checkNotNull(this)
    fun Appendable.appendGenerics() {
        if (element == null) return
        val typeArguments = element!!.typeArguments
        if (typeArguments.isEmpty()) return
        append('<')
        typeArguments.forEach({ append(", ") }) { typeArgument ->
            val variance = typeArgument.variance
            append(variance.label)
            when (variance) {
                Variance.STAR -> return@forEach
                Variance.INVARIANT -> {}
                Variance.COVARIANT, Variance.CONTRAVARIANT -> append(' ')
            }
            append(typeArgument.type.toType())
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
     * TODO: KSP generated code is platform code (see https://github.com/google/ksp/issues/2233).
     * This mode only works for platform code because common/intermediate code cannot reference platform code.
     */
    InBuildDir,

    /** Generate the code in the code repository. */
    InRepository,
}

/**
 * KSP argument for setting the [GenerateMode].
 * Uses [GenerateMode.InBuildDir] if not specified.
 */
public const val GENERATE_MODE: String = "yass.GenerateMode"

private fun processPackage(packageName: String, declarations: Set<KSDeclaration>, environment: SymbolProcessorEnvironment) {
    fun annotationOrNull(annotation: KClass<*>) = declarations
        .mapNotNull { declaration ->
            declaration.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName() == annotation.qualifiedName }
        }
        .also { annotations ->
            require(annotations.size <= 1) { "there can be at most one annotation '${annotation.qualifiedName}' in package '$packageName'" }
        }
        .firstOrNull()

    val code = StringBuilder()
    code.appendPackage(packageName)
    val writer = CodeWriter(code)

    annotationOrNull(Proxies::class)?.let {
        it.value().forEach { type -> writer.generateProxy(type.declaration as KSClassDeclaration) }
    }

    val concreteAndEnumClasses = annotationOrNull(ConcreteAndEnumClasses::class)
    val binaryEncoders = annotationOrNull(BinaryEncoders::class)
    val stringEncoders = annotationOrNull(StringEncoders::class)
    if (concreteAndEnumClasses == null) {
        require(binaryEncoders == null && stringEncoders == null) {
            "missing annotation '${ConcreteAndEnumClasses::class.qualifiedName}' in package '$packageName'"
        }
    } else {
        require(binaryEncoders != null || stringEncoders != null) {
            "missing annotations '${BinaryEncoders::class.qualifiedName}' or '${StringEncoders::class.qualifiedName}' in package '$packageName'"
        }
        binaryEncoders?.let { writer.generateBinarySerializer(it.value(), concreteAndEnumClasses.value()) }
        stringEncoders?.let { writer.generateStringEncoders(it.value(), concreteAndEnumClasses.value()) }
    }

    val generateMode = GenerateMode.valueOf(environment.options[GENERATE_MODE] ?: GenerateMode.InBuildDir.name)
    if (generateMode == GenerateMode.InRepository) {
        writeGeneratedFile(Path((declarations.first().location as FileLocation).filePath).parent, code.toString())
    } else {
        environment.codeGenerator
            .createNewFile(Dependencies.ALL_FILES, packageName, GENERATED_BY_YASS)
            .writer()
            .use { it.append(code) }
    }
}

internal class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = object : SymbolProcessor {
        override fun process(resolver: Resolver): List<KSAnnotated> {
            listOf(
                Proxies::class,
                ConcreteAndEnumClasses::class,
                BinaryEncoders::class,
                StringEncoders::class,
            )
                .flatMap { annotation -> resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!) }
                .map { annotated -> annotated as KSDeclaration }
                .groupBy { declaration -> declaration.packageName.asString() }
                .forEach { [packageName, declarations] -> processPackage(packageName, declarations.toSet(), environment) }
            return emptyList()
        }
    }
}

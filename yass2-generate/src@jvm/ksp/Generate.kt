@file:OptIn(InternalYassApi::class)

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.GenerateInRepository
import ch.softappeal.yass2.core.InternalYassApi
import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.forEachSeparator
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.writeGeneratedFile
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
        typeArguments.forEachSeparator({ append(", ") }) { typeArgument ->
            val variance = typeArgument.variance
            append(variance.label)
            when (variance) {
                Variance.STAR -> return@forEachSeparator
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

private fun processPackage(packageName: String, declarations: Set<KSDeclaration>, environment: SymbolProcessorEnvironment) {
    fun annotationOrNull(annotation: KClass<*>) = declarations
        .mapNotNull { declaration ->
            declaration.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName() == annotation.qualifiedName }
        }
        .also { annotations ->
            require(annotations.size <= 1) { "there can be at most one annotation '${annotation.qualifiedName}' in package '$packageName'" }
        }
        .firstOrNull()

    val generateInRepository = annotationOrNull(GenerateInRepository::class) != null

    val code = StringBuilder()
    code.appendPackage(packageName)
    val writer = CodeWriter(code)

    annotationOrNull(Proxies::class)?.let {
        it.value().forEach { type -> writer.generateProxy(type.declaration as KSClassDeclaration) }
    }

    val concreteAndEnumClasses = annotationOrNull(ConcreteAndEnumClasses::class)
    val binaryEncoderObjects = annotationOrNull(BinaryEncoderObjects::class)
    val stringEncoderObjects = annotationOrNull(StringEncoderObjects::class)
    if (concreteAndEnumClasses == null) {
        require(binaryEncoderObjects == null && stringEncoderObjects == null) {
            "missing annotation '${ConcreteAndEnumClasses::class.qualifiedName}' in package '$packageName'"
        }
    } else {
        require(binaryEncoderObjects != null || stringEncoderObjects != null) {
            "missing annotations '${BinaryEncoderObjects::class.qualifiedName}' or '${StringEncoderObjects::class.qualifiedName}' in package '$packageName'"
        }
        binaryEncoderObjects?.let { writer.generateBinarySerializer(it.value(), concreteAndEnumClasses.value()) }
        stringEncoderObjects?.let { writer.generateStringEncoders(it.value(), concreteAndEnumClasses.value()) }
    }

    if (generateInRepository) {
        writeGeneratedFile(Path((declarations.first().location as FileLocation).filePath).parent, code.toString())
    } else {
        environment.codeGenerator
            .createNewFile(Dependencies.ALL_FILES, packageName, GENERATED_BY_YASS)
            .writer()
            .use { it.append(code) }
    }
}

/** @suppress */
@InternalYassApi public class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = object : SymbolProcessor {
        override fun process(resolver: Resolver): List<KSAnnotated> {
            listOf(
                GenerateInRepository::class,
                Proxies::class,
                ConcreteAndEnumClasses::class,
                BinaryEncoderObjects::class,
                StringEncoderObjects::class,
            )
                .flatMap { annotation -> resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!) }
                .map { annotated -> annotated as KSDeclaration }
                .groupBy { declaration -> declaration.packageName.asString() }
                .forEach { [packageName, declarations] -> processPackage(packageName, declarations.toSet(), environment) }
            return emptyList()
        }
    }
}

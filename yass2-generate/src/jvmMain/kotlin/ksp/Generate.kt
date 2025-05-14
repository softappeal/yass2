package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.Proxy
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
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
        typeArguments.forEachIndexed { typeArgumentIndex, typeArgument ->
            if (typeArgumentIndex != 0) append(", ")
            val variance = typeArgument.variance
            append(variance.label)
            when (variance) {
                Variance.STAR -> return@forEachIndexed
                Variance.INVARIANT -> {}
                Variance.COVARIANT, Variance.CONTRAVARIANT -> append(' ')
            }
            append(typeArgument.type!!.toType())
        }
        append('>')
    }

    val type = resolve()
    val appendable = StringBuilder()
    if (type.declaration is KSTypeParameter) {
        appendable.append(type.declaration.name)
    } else {
        appendable.append(type.qualifiedName).appendGenerics()
    }
    if (type.isMarkedNullable) appendable.append('?')
    return appendable.toString()
}

private fun KSAnnotated.annotationOrNull(annotation: KClass<*>) =
    annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName() == annotation.qualifiedName }

@Suppress("UNCHECKED_CAST")
private fun KSAnnotation.value() = arguments.first { it.name!!.asString() == "value" }.value as List<KSType>

private class Yass2Processor(@Suppress("unused") environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {

        val packageNameToDeclarations = buildMap {
            buildList {
                listOf(
                    Proxy::class, ConcreteAndEnumClasses::class, BinaryEncoderObjects::class, StringEncoderObjects::class,
                ).forEach { annotation ->
                    resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!)
                        .map { it as KSDeclaration }
                        .forEach { declaration -> add(declaration.packageName.asString() to declaration) }
                }
            }.groupBy({ it.first }, { it.second })
                .forEach { packageName, declarations -> put(packageName, declarations.toSet()) }
        }

        packageNameToDeclarations.forEach { packageName, declarations ->
            val code = StringBuilder()
            code.appendPackage(packageName)
            @OptIn(InternalApi::class) val writer = CodeWriter(code)

            buildList {
                declarations.forEach { declaration ->
                    declaration.annotationOrNull(Proxy::class)?.let { add(declaration) }
                }
            }.sortedBy { it.name }
                .forEach { declaration -> writer.generateProxy(declaration as KSClassDeclaration) }

            fun declarationsAnnotationOrNull(annotation: KClass<*>): KSAnnotation? {
                val annotations = buildList {
                    declarations.forEach { declaration ->
                        declaration.annotationOrNull(annotation)?.let { annotation -> add(annotation) }
                    }
                }
                require(annotations.size <= 1) {
                    "there can be at most one annotation '${annotation.qualifiedName}' in package '$packageName'"
                }
                return annotations.firstOrNull()
            }

            declarationsAnnotationOrNull(ConcreteAndEnumClasses::class)?.let { annotation ->
                val concreteAndEnumClasses = annotation.value()
                declarationsAnnotationOrNull(BinaryEncoderObjects::class)?.let { annotation ->
                    writer.generateBinarySerializer(annotation.value(), concreteAndEnumClasses)
                }
                declarationsAnnotationOrNull(StringEncoderObjects::class)?.let { annotation ->
                    writer.generateStringEncoders(annotation.value(), concreteAndEnumClasses)
                }
            }

            // TODO: We don't use 'createNewFile()' and write directly to the source location without expect/actual (see https://github.com/google/ksp/issues/2233).
            //       Common/intermediate (= none-platform) code cannot reference generated code in the compilation of platform code.
            //       Generated codes are treated as platform code (you'll have to use expect/actual).
            val declarationPath = Path((declarations.first().location as FileLocation).filePath)
            declarationPath.parent.resolve("$GENERATED_BY_YASS.kt").writeText(code)
        }
        return emptyList()
    }
}

@InternalApi
public class Yass2Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = Yass2Processor(environment)
}

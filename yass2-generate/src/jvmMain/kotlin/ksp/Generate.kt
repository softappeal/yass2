package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.serialize.GenerateSerializer
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import kotlin.reflect.KClass

internal fun KSType.isEnum() = (declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS

internal fun KSDeclaration.qualifiedName() = qualifiedName!!.asString()
internal val KSDeclaration.name get() = simpleName.asString()
internal val KSType.qualifiedName get() = declaration.qualifiedName()

internal fun KSTypeReference.type(): String {
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
            append(typeArgument.type!!.type())
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

private fun KSAnnotation.argument(name: String) = arguments.first { it.name!!.asString() == name }.value!!

private val Platforms = setOf(
    "jvm", // JVM
    "js", // JavaScript
    "wasmJs", "wasmWasi",// WebAssembly
    "macosX64", "macosArm64", // macOS
    "iosArm64", "iosX64", "iosSimulatorArm64", // iOS
    "linuxX64", "linuxArm64", // Linux
    "watchosArm64", "watchosX64", "watchosSimulatorArm64", "watchosDeviceArm64", // watchOS
    "tvosArm64", "tvosX64", "tvosSimulatorArm64", // tvOS
    "mingwX64", // Windows
)

private fun KSDeclaration.isPlatformCode(): Boolean { // TODO: is there a better solution?
    val filePath = containingFile!!.filePath
    return Platforms.any { platform -> filePath.contains("/${platform}Main/") || filePath.contains("/${platform}Test/") }
}

internal fun KSDeclaration.actual() = if (isPlatformCode()) "" else "actual "

private fun KSDeclaration.annotationOrNull(annotation: KClass<*>) =
    annotations.firstOrNull { it.shortName.asString() == annotation.simpleName }

private class AnnotatedDeclaration(val declaration: KSPropertyDeclaration, val annotation: KSAnnotation)

private class Yass2Processor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val packageName2declarations = buildList {
            listOf(GenerateProxy::class, GenerateSerializer::class).forEach { annotation ->
                resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!)
                    .map { it as KSDeclaration }
                    .forEach { declaration -> add(declaration.packageName.asString() to declaration) }
            }
        }.groupBy({ it.first }, { it.second })
        packageName2declarations.forEach { (packageName, declarations) ->
            @Suppress("UNCHECKED_CAST")
            codeGenerator.createNewFile(
                Dependencies.ALL_FILES, // we want to be on the safe side
                packageName,
                GENERATED_BY_YASS,
            ).writer().use { writer ->
                writer.appendPackage(packageName)
                val codeWriter = CodeWriter(writer)

                buildList {
                    declarations.forEach { declaration ->
                        declaration.annotationOrNull(GenerateProxy::class)?.let { add(declaration) }
                    }
                }.sortedBy { it.name }.forEach { declaration -> codeWriter.generateProxy(declaration as KSClassDeclaration) }

                fun List<KSDeclaration>.annotatedDeclarationOrNull(annotation: KClass<*>): AnnotatedDeclaration? {
                    val annotatedDeclaration = buildList {
                        this@annotatedDeclarationOrNull.forEach { declaration ->
                            declaration.annotationOrNull(annotation)?.let { annotation ->
                                add(AnnotatedDeclaration(declaration as KSPropertyDeclaration, annotation))
                            }
                        }
                    }
                    require(annotatedDeclaration.size <= 1) {
                        "there can be at most one annotation ${annotation.simpleName} in package $packageName @${annotatedDeclaration.first().annotation.location}"
                    }
                    return annotatedDeclaration.firstOrNull()
                }

                val serializer = declarations.annotatedDeclarationOrNull(GenerateSerializer::class)
                if (serializer != null) {
                    val binaryEncoderClasses = serializer.annotation.argument("binaryEncoderClasses") as List<KSType>
                    val textEncoderClasses = serializer.annotation.argument("textEncoderClasses") as List<KSType>
                    val concreteClasses = serializer.annotation.argument("concreteClasses") as List<KSType>
                    codeWriter.generateSerializer(
                        binaryEncoderClasses,
                        textEncoderClasses,
                        concreteClasses.filter { it.isEnum() },
                        concreteClasses.filterNot { it.isEnum() },
                        serializer.declaration
                    )
                }
            }
        }
        return emptyList()
    }
}

public class Yass2Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = Yass2Processor(environment)
}

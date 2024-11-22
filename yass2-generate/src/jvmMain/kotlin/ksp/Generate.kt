package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
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
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Variance
import kotlin.reflect.KClass

internal fun KSType.isEnum() = (declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS

internal fun checkNotEnum(location: Location, classes: List<KSType>, message: String) {
    classes.firstOrNull { it.isEnum() }?.let { error("enum class ${it.qualifiedName} $message @$location") }
}

internal fun List<KSType>.checkNotDuplicated(location: Location) {
    require(hasNoDuplicates()) { "classes ${duplicates()} are duplicated @$location" }
}

internal fun KSClassDeclaration.getAllPropertiesNotThrowable() = getAllProperties().toList()
    .filterNot { (it.name == "cause" || it.name == "message") && ("kotlin.Throwable" == it.parentDeclaration!!.qualifiedName()) }
    .sortedBy { it.name }

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

// TODO: default values in annotations don't yet work for multiplatform libraries;
//       see https://youtrack.jetbrains.com/issue/KT-59526/Store-annotation-default-values-in-metadata-on-JVM
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

private fun KSDeclaration.isPlatform(): Boolean { // TODO: is there a better solution?
    val filePath = containingFile!!.filePath
    Platforms.forEach { platform ->
        if (filePath.contains("/${platform}Main/") || filePath.contains("/${platform}Test/")) return true
    }
    return false
}

internal fun KSDeclaration.actual() = if (isPlatform()) "" else "actual "

private fun KSDeclaration.annotationOrNull(annotation: KClass<*>) =
    annotations.firstOrNull { it.shortName.asString() == annotation.simpleName }

private class AnnotatedDeclaration(val declaration: KSDeclaration, val annotation: KSAnnotation)

private class Yass2Processor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val packageName2declarations = buildList {
            listOf(GenerateProxy::class, GenerateBinarySerializer::class, GenerateDumper::class).forEach { annotation ->
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
                                add(AnnotatedDeclaration(declaration, annotation))
                            }
                        }
                    }
                    require(annotatedDeclaration.size <= 1) {
                        "there can be at most one annotation ${annotation.simpleName} in package $packageName @${annotatedDeclaration.first().annotation.location}"
                    }
                    return annotatedDeclaration.firstOrNull()
                }

                val serializer = declarations.annotatedDeclarationOrNull(GenerateBinarySerializer::class)
                val dumper = declarations.annotatedDeclarationOrNull(GenerateDumper::class)
                require((dumper == null) || (serializer == null) || !(serializer.annotation.argument("withDumper") as Boolean)) {
                    "illegal use of annotations ${GenerateBinarySerializer::class.simpleName} and ${GenerateDumper::class.simpleName} in package $packageName @${serializer!!.annotation.location}"
                }

                if (serializer != null) {
                    val baseEncoderClasses = serializer.annotation.argument("baseEncoderClasses") as List<KSType>
                    val enumClasses = serializer.annotation.argument("enumClasses") as List<KSType>
                    val treeConcreteClasses = serializer.annotation.argument("treeConcreteClasses") as List<KSType>
                    val graphConcreteClasses = serializer.annotation.argument("graphConcreteClasses") as List<KSType>
                    val withDumper = serializer.annotation.argument("withDumper") as Boolean
                    codeWriter.generateBinarySerializer(
                        baseEncoderClasses, enumClasses, treeConcreteClasses, graphConcreteClasses,
                        serializer.declaration.actual(), serializer.annotation.location,
                    )
                    if (withDumper) codeWriter.generateDumper(
                        treeConcreteClasses, graphConcreteClasses, serializer.declaration.actual(), serializer.annotation.location
                    )
                }

                if (dumper != null) {
                    val treeConcreteClasses = dumper.annotation.argument("treeConcreteClasses") as List<KSType>
                    val graphConcreteClasses = dumper.annotation.argument("graphConcreteClasses") as List<KSType>
                    codeWriter.generateDumper(
                        treeConcreteClasses, graphConcreteClasses, dumper.declaration.actual(), dumper.annotation.location
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

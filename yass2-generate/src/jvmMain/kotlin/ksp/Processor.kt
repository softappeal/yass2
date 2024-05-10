package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.generate.GENERATED_BINARY_SERIALIZER
import ch.softappeal.yass2.generate.GENERATED_DUMPER
import ch.softappeal.yass2.generate.GENERATED_PROXY
import ch.softappeal.yass2.generate.appendPackage
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import com.google.devtools.ksp.getAllSuperTypes
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
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import kotlin.reflect.KClass

internal fun KSDeclaration.qualifiedName() = qualifiedName!!.asString()
internal val KSDeclaration.name get() = simpleName.asString()
internal val KSType.qualifiedName get() = declaration.qualifiedName()

internal fun KSClassDeclaration.getAllPropertiesNotThrowable(): List<KSPropertyDeclaration> {
    fun KSPropertyDeclaration.isPropertyOfThrowable(): Boolean {
        return (name == "cause" || name == "message") &&
            "kotlin.Throwable" in (parentDeclaration as KSClassDeclaration).getAllSuperTypes().map { it.qualifiedName }
    }
    return getAllProperties()
        .toList()
        .filterNot { it.isPropertyOfThrowable() }
        .sortedBy { it.name }
}

internal fun Appendable.appendType(typeReference: KSTypeReference): Appendable {
    fun Appendable.appendGenerics() {
        val element =
            typeReference.element ?: error("generic type '$typeReference' must not be implicit @${typeReference.parent?.location}")
        val typeArguments = element.typeArguments
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
            appendType(typeArgument.type!!)
        }
        append('>')
    }

    val type = typeReference.resolve()
    append(type.qualifiedName).appendGenerics()
    if (type.isMarkedNullable) append('?')
    return this
}

internal fun List<KSType>.getBaseEncoderTypes() =
    map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }

// NOTE: default values in annotations don't yet work for multiplatform libraries;
//       see https://youtrack.jetbrains.com/issue/KT-59526/Store-annotation-default-values-in-metadata-on-JVM
private fun KSAnnotation.argument(name: String) = arguments.first { it.name!!.asString() == name }.value!!

private fun KSAnnotation.checkClasses(classes: List<KSType>, enumMessage: String) {
    fun KSType.isEnum() = (declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS
    require(classes.size == classes.toSet().size) { "class must not be duplicated @$location" }
    classes.firstOrNull { it.isEnum() }?.let { klass -> error("enum class '${klass.qualifiedName}' $enumMessage @$location") }
}

private class Yass2Processor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    private val enableLogging = (environment.options["yass2.enableLogging"] ?: "false").toBooleanStrict()

    fun log(message: String, symbol: KSNode? = null) {
        if (enableLogging) logger.warn(message, symbol) // is 'warn' instead of 'info' because 'info' is not printed by default
    }

    init {
        log("processor '${Yass2Processor::class.qualifiedName}' created")
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        log("process() called")

        fun Resolver.getSymbolsWithAnnotation(annotation: KClass<*>): Sequence<KSAnnotated> {
            log("getSymbolsWithAnnotation('${annotation.qualifiedName}') called")
            return getSymbolsWithAnnotation(annotation.qualifiedName!!).onEach {
                log("symbol '$it' returned", it)
            }
        }

        fun generate(file: String, packageName: String, generate: Appendable.() -> Unit) {
            codeGenerator.createNewFile(
                Dependencies.ALL_FILES, // we want to be on the safe side
                packageName,
                file,
            ).writer().use { appendable ->
                appendable.appendPackage(packageName)
                appendable.generate()
            }
        }

        buildList {
            resolver.getSymbolsWithAnnotation(GenerateProxy::class)
                .map { annotated -> annotated as KSClassDeclaration }
                .forEach { classDeclaration -> add(Pair(classDeclaration.packageName.asString(), classDeclaration)) }
        }.groupBy({ it.first }, { it.second }).entries.forEach { (packageName, services) ->
            generate(GENERATED_PROXY, packageName) {
                services.sortedBy { it.name }.forEach { generateProxy(it) }
            }
        }

        val usedPackages = mutableSetOf<String>()
        fun forEachAnnotation(generateAnnotation: KClass<*>, block: (packageName: String, annotation: KSAnnotation) -> Unit) {
            buildMap {
                resolver.getSymbolsWithAnnotation(generateAnnotation)
                    .map { annotated -> annotated as KSPropertyDeclaration }
                    .forEach { file ->
                        file.annotations
                            .filter { annotation -> annotation.shortName.asString() == generateAnnotation.simpleName }
                            .forEach { annotation ->
                                val packageName = file.packageName.asString()
                                require(put(packageName, annotation) == null) {
                                    "annotation '${generateAnnotation.qualifiedName}' must not be duplicated in " +
                                        "package '$packageName' @${annotation.location}"
                                }
                                require(usedPackages.add(packageName)) {
                                    "annotation '${GenerateBinarySerializer::class.qualifiedName}' and '${GenerateDumper::class.qualifiedName}' " +
                                        "must not be duplicated in package '$packageName' @${annotation.location}"
                                }
                            }
                    }
            }.entries.forEach { (packageName, annotation) -> block(packageName, annotation) }
        }

        @Suppress("UNCHECKED_CAST")
        forEachAnnotation(GenerateBinarySerializer::class) { packageName, annotation ->
            val baseEncoderClasses = annotation.argument("baseEncoderClasses") as List<KSType>
            val enumClasses = annotation.argument("enumClasses") as List<KSType>
            val treeConcreteClasses = annotation.argument("treeConcreteClasses") as List<KSType>
            val graphConcreteClasses = annotation.argument("graphConcreteClasses") as List<KSType>
            val withDumper = annotation.argument("withDumper") as Boolean
            require(enumClasses.size == enumClasses.toSet().size) { "enum classes must not be duplicated @${annotation.location}" }
            annotation.checkClasses(
                baseEncoderClasses.getBaseEncoderTypes() + treeConcreteClasses + graphConcreteClasses,
                "belongs to 'enumClasses'"
            )
            generate(GENERATED_BINARY_SERIALIZER, packageName) {
                generateBinarySerializer(baseEncoderClasses, enumClasses, treeConcreteClasses, graphConcreteClasses)
            }
            if (withDumper) generate(GENERATED_DUMPER, packageName) { generateDumper(treeConcreteClasses, graphConcreteClasses) }
        }

        @Suppress("UNCHECKED_CAST")
        forEachAnnotation(GenerateDumper::class) { packageName, annotation ->
            val treeConcreteClasses = annotation.argument("treeConcreteClasses") as List<KSType>
            val graphConcreteClasses = annotation.argument("graphConcreteClasses") as List<KSType>
            annotation.checkClasses(treeConcreteClasses + graphConcreteClasses, "must not be specified")
            generate(GENERATED_DUMPER, packageName) { generateDumper(treeConcreteClasses, graphConcreteClasses) }
        }

        return emptyList()
    }
}

public class Yass2Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = Yass2Processor(environment)
}

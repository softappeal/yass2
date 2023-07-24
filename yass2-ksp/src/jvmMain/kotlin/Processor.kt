package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.*
import ch.softappeal.yass2.serialize.binary.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import kotlin.reflect.*

public const val GENERATED_PROXY: String = "GeneratedProxy"
public const val GENERATED_BINARY_SERIALIZER: String = "GeneratedBinarySerializer"
public const val GENERATED_DUMPER: String = "GeneratedDumper"

internal const val CSY = "ch.softappeal.yass2"

internal fun Appendable.append(level: Int, s: String): Appendable {
    append("    ".repeat(level)).append(s)
    return this
}

internal fun Appendable.appendLine(level: Int, s: String) {
    append(level, s).appendLine()
}

internal fun KSDeclaration.qualifiedName() = qualifiedName!!.asString()
internal fun KSDeclaration.simpleName() = simpleName.asString()
internal fun KSType.qualifiedName() = declaration.qualifiedName()

private fun KSPropertyDeclaration.isPropertyOfThrowable(): Boolean {
    val name = simpleName()
    return (name == "cause" || name == "message") && "kotlin.Throwable" in (parentDeclaration as KSClassDeclaration).getAllSuperTypes().map { it.qualifiedName() }
}

internal fun KSClassDeclaration.getAllPropertiesNotThrowable() = getAllProperties()
    .toList()
    .filterNot { it.isPropertyOfThrowable() }
    .sortedBy { it.simpleName() }

internal fun Appendable.appendType(typeReference: KSTypeReference): Appendable {
    fun Appendable.appendGenerics() {
        val typeArguments = (typeReference.element ?: error("generic type '$typeReference' must not be implicit @${typeReference.parent?.location}")).typeArguments
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
    append(type.qualifiedName()).appendGenerics()
    if (type.isMarkedNullable) append('?')
    return this
}

internal fun List<KSType>.getBaseEncoderTypes() = map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }

private fun KSType.isEnum() = (declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS

// NOTE: default values in annotations don't yet work for multiplatform libraries; see https://youtrack.jetbrains.com/issue/KT-59566/Annotation-default-value-is-not-populated-in-metadata-for-libraries
private fun KSAnnotation.argument(name: String) = arguments.first { it.name!!.asString() == name }.value!!

private fun KSAnnotation.checkClasses(classes: List<KSType>, message: String) {
    require(classes.size == classes.toSet().size) { "class must not be duplicated @$location" }
    classes.firstOrNull { it.isEnum() }?.let { klass ->
        error("enum class '${klass.qualifiedName()}' $message @$location")
    }
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
                appendable.appendLine("""
                    @file:Suppress(
                        "UNCHECKED_CAST",
                        "USELESS_CAST",
                        "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
                        "unused",
                        "RemoveRedundantQualifierName",
                        "SpellCheckingInspection",
                        "RedundantVisibilityModifier",
                        "RedundantNullableReturnType",
                        "KotlinRedundantDiagnosticSuppress",
                        "RedundantSuppression",
                    )

                    package $packageName
                """.trimIndent())
                appendable.generate()
            }
        }

        buildList {
            resolver.getSymbolsWithAnnotation(GenerateProxy::class)
                .map { annotated -> annotated as KSClassDeclaration }
                .forEach { classDeclaration -> add(Pair(classDeclaration.packageName.asString(), classDeclaration)) }
        }.groupBy({ it.first }, { it.second }).entries.forEach { (packageName, services) ->
            generate(GENERATED_PROXY, packageName) {
                services.sortedBy { it.qualifiedName() }.forEach { generateProxy(it) }
            }
        }

        val usedPackages = mutableSetOf<String>()
        fun forEachFileAnnotation(generateAnnotation: KClass<*>, block: (packageName: String, annotation: KSAnnotation) -> Unit) {
            buildMap {
                resolver.getSymbolsWithAnnotation(generateAnnotation)
                    .map { annotated -> annotated as KSFile }
                    .forEach { file ->
                        file.annotations
                            .filter { annotation -> annotation.shortName.asString() == generateAnnotation.simpleName }
                            .forEach { annotation ->
                                val packageName = file.packageName.asString()
                                require(put(packageName, annotation) == null) {
                                    "annotation '${generateAnnotation.qualifiedName}' must not be duplicated in package '$packageName' @${annotation.location}"
                                }
                                require(usedPackages.add(packageName)) {
                                    "annotation '${GenerateBinarySerializer::class.qualifiedName}' and '${GenerateDumper::class.qualifiedName}' must not be duplicated in package '$packageName' @${annotation.location}"
                                }
                            }
                    }
            }.entries.forEach { (packageName, annotation) -> block(packageName, annotation) }
        }

        @Suppress("UNCHECKED_CAST")
        forEachFileAnnotation(GenerateBinarySerializer::class) { packageName, annotation ->
            val baseEncoderClasses = annotation.argument("baseEncoderClasses") as List<KSType>
            var treeConcreteClasses = annotation.argument("treeConcreteClasses") as List<KSType>
            val graphConcreteClasses = annotation.argument("graphConcreteClasses") as List<KSType>
            val withDumper = annotation.argument("withDumper") as Boolean
            val enumClasses = treeConcreteClasses.filter { it.isEnum() }
            require(enumClasses.size == enumClasses.toSet().size) { "enum classes must not be duplicated @${annotation.location}" }
            treeConcreteClasses = treeConcreteClasses - enumClasses.toSet()
            annotation.checkClasses(baseEncoderClasses.getBaseEncoderTypes() + treeConcreteClasses + graphConcreteClasses, "belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses'")
            generate(GENERATED_BINARY_SERIALIZER, packageName) { generateBinarySerializer(baseEncoderClasses, treeConcreteClasses, graphConcreteClasses, enumClasses) }
            if (withDumper) generate(GENERATED_DUMPER, packageName) { generateDumper(treeConcreteClasses, graphConcreteClasses) }
        }

        @Suppress("UNCHECKED_CAST")
        forEachFileAnnotation(GenerateDumper::class) { packageName, annotation ->
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

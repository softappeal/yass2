package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
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

private class YassProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    private val enableLogging = (environment.options["enableLogging"] ?: "false").toBooleanStrict()

    fun log(message: String, symbol: KSNode? = null) {
        if (enableLogging) logger.warn(message, symbol) // is 'warn' instead of 'info' because 'info' is not printed by default
    }

    init {
        log("processor '${YassProcessor::class.qualifiedName}' created")
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
            resolver.getSymbolsWithAnnotation(Proxy::class)
                .map { annotated -> annotated as KSClassDeclaration }
                .forEach { classDeclaration -> add(Pair(classDeclaration.packageName.asString(), classDeclaration)) }
        }.groupBy({ it.first }, { it.second }).entries.forEach { (packageName, services) ->
            generate(GENERATED_PROXY, packageName) {
                services.sortedBy { it.qualifiedName() }.forEach { generateProxy(it) }
            }
        }

        buildMap {
            resolver.getSymbolsWithAnnotation(BinarySerializerAndDumper::class)
                .map { annotated -> annotated as KSFile }
                .forEach { file ->
                    file.annotations
                        .filter { annotation -> annotation.shortName.asString() == BinarySerializerAndDumper::class.simpleName }
                        .forEach { annotation ->
                            val packageName = file.packageName.asString()
                            require(put(packageName, annotation) == null) {
                                "annotation '${BinarySerializerAndDumper::class.qualifiedName}' must not be duplicated in package '$packageName' @${annotation.location}"
                            }
                        }
                }
        }.entries.forEach { (packageName, annotation) ->
            fun argument(index: Int): List<KSType> {
                var value = annotation.arguments[index].value
                if (value == null) value = emptyList<KSType>() // NOTE: seems to be a bug in KSP for some platforms
                @Suppress("UNCHECKED_CAST") return value as List<KSType>
            }

            val baseEncoderClasses = argument(0)
            var treeConcreteClasses = argument(1)
            val graphConcreteClasses = argument(2)
            val enumClasses = treeConcreteClasses.filter { it.isEnum() }
            require(enumClasses.size == enumClasses.toSet().size) { "enum classes must not be duplicated @${annotation.location}" }
            treeConcreteClasses = treeConcreteClasses - enumClasses.toSet()
            val encoderTypes = baseEncoderClasses.getBaseEncoderTypes() + treeConcreteClasses + graphConcreteClasses
            require(encoderTypes.size == encoderTypes.toSet().size) { "encoder type must not be duplicated @${annotation.location}" }
            encoderTypes.firstOrNull { it.isEnum() }?.let { enumType ->
                error("enum class '${enumType.qualifiedName()}' belongs to 'treeConcreteClasses' and not to 'baseEncoderClasses' or 'graphConcreteClasses' @${annotation.location}")
            }
            generate(GENERATED_BINARY_SERIALIZER, packageName) { generateBinarySerializer(baseEncoderClasses, treeConcreteClasses, graphConcreteClasses, enumClasses) }
            generate(GENERATED_DUMPER, packageName) { generateDumper(treeConcreteClasses, graphConcreteClasses) }
        }

        return emptyList()
    }
}

public class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = YassProcessor(environment)
}

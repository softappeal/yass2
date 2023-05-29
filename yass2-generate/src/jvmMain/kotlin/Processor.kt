package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

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
        val typeArguments = typeReference.element!!.typeArguments
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

private class YassProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        fun generate(file: String, packageName: String, generate: Appendable.() -> Unit) {
            codeGenerator.createNewFile(Dependencies(true, *resolver.getAllFiles().toList().toTypedArray()), packageName, file).writer().use { appendable ->
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

        val invalidSymbols = mutableListOf<KSAnnotated>()

        fun Sequence<KSAnnotated>.handleValidate() = filter {
            if (it.validate()) {
                true
            } else {
                invalidSymbols.add(it)
                false
            }
        }

        buildList {
            resolver.getSymbolsWithAnnotation(GenerateProxy::class.qualifiedName!!)
                .handleValidate()
                .map { annotated -> annotated as KSClassDeclaration }
                .forEach { classDeclaration -> add(Pair(classDeclaration.packageName.asString(), classDeclaration)) }
        }.groupBy({ it.first }, { it.second }).entries.forEach { (packageName, services) ->
            generate(GENERATED_PROXY, packageName) {
                services.sortedBy { it.qualifiedName() }.forEach { generateProxy(it) }
            }
        }

        buildMap {
            resolver.getSymbolsWithAnnotation(GenerateBinarySerializerAndDumper::class.qualifiedName!!)
                .handleValidate()
                .map { annotated -> annotated as KSFile }
                .forEach { file ->
                    file.annotations
                        .filter { annotation -> annotation.shortName.asString() == GenerateBinarySerializerAndDumper::class.simpleName }
                        .forEach { annotation ->
                            val packageName = file.packageName.asString()
                            require(put(packageName, annotation) == null) {
                                "annotation '${GenerateBinarySerializerAndDumper::class.qualifiedName}' must not be duplicated in package '$packageName' @${annotation.location}"
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
            val treeConcreteClasses = argument(1)
            val graphConcreteClasses = argument(2)
            val encoderTypes = baseEncoderClasses.getBaseEncoderTypes() + treeConcreteClasses + graphConcreteClasses
            require(encoderTypes.size == encoderTypes.toSet().size) { "encoder type must not be duplicated @${annotation.location}" }
            generate(GENERATED_BINARY_SERIALIZER, packageName) { generateBinarySerializer(baseEncoderClasses, treeConcreteClasses, graphConcreteClasses) }
            generate(GENERATED_DUMPER, packageName) { generateDumper(treeConcreteClasses, graphConcreteClasses) }
        }

        return invalidSymbols
    }
}

public class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = YassProcessor(environment)
}

package ch.softappeal.yass2.generate // TODO: review all files in this directory

import ch.softappeal.yass2.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

internal fun Appendable.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal const val CSY = "ch.softappeal.yass2"

private fun Appendable.writeHeader(packageName: String) {
    appendLine("""
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
}

public const val GENERATED_PROXY: String = "GeneratedProxy"
public const val GENERATED_BINARY_SERIALIZER: String = "GeneratedBinarySerializer"
public const val GENERATED_DUMPER: String = "GeneratedDumper"

internal fun KSDeclaration.name() = qualifiedName!!.asString()

internal fun Appendable.appendType(typeReference: KSTypeReference) {
    fun KSTypeReference.appendGenerics() {
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
            appendType(typeArgument.type!!)
        }
        append('>')
    }

    val type = typeReference.resolve()
    append(type.declaration.name())
    typeReference.appendGenerics()
    if (type.isMarkedNullable) append('?')
}

internal fun KSPropertyDeclaration.isPropertyOfThrowable(): Boolean {
    val name = simpleName.asString()
    return (name == "cause" || name == "message") && "kotlin.Throwable" in (parentDeclaration as KSClassDeclaration).getAllSuperTypes().map { it.declaration.name() }
}

public class YassProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {
        fun generate(packageName: String, file: String, generate: Appendable.() -> Unit) {
            codeGenerator.createNewFile(Dependencies(false), packageName, file).writer().use { writer ->
                writer.writeHeader(packageName)
                writer.generate()
            }
        }

        buildMap {
            resolver.getSymbolsWithAnnotation(GenerateBinarySerializerAndDumper::class.qualifiedName!!).forEach { annotated ->
                val file = annotated as KSFile
                file.annotations.forEach { annotation ->
                    if (annotation.shortName.asString() == GenerateBinarySerializerAndDumper::class.simpleName) {
                        val packageName = file.packageName.asString()
                        check(put(packageName, annotation) == null) {
                            "duplicated annotation ${GenerateBinarySerializerAndDumper::class.qualifiedName} in package $packageName: ${annotation.location}"
                        }
                    }
                }
            }
        }.entries.forEach { (packageName, annotation) ->
            fun argument(index: Int): List<KSType> {
                var value = annotation.arguments[index].value
                if (value == null) value = emptyList<KSType>() // TODO: seems to be a bug in KSP for some platforms
                @Suppress("UNCHECKED_CAST") return value as List<KSType>
            }

            val baseEncoderClasses = argument(0)
            val treeConcreteClasses = argument(1)
            val graphConcreteClasses = argument(2)
            val concreteClasses = treeConcreteClasses + graphConcreteClasses
            require(concreteClasses.size == concreteClasses.toSet().size) { "duplicated concrete class" }
            val encoderTypes = baseEncoderClasses.map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() } + concreteClasses
            require(encoderTypes.size == encoderTypes.toSet().size) { "duplicated encoder type" }
            generate(packageName, GENERATED_BINARY_SERIALIZER) { generateBinarySerializer(baseEncoderClasses, treeConcreteClasses, graphConcreteClasses) }
            generate(packageName, GENERATED_DUMPER) { generateDumper(treeConcreteClasses, graphConcreteClasses) }
        }

        buildList {
            resolver.getSymbolsWithAnnotation(GenerateProxy::class.qualifiedName!!)
                .forEach { add(Pair((it as KSClassDeclaration).packageName.asString(), it)) }
        }.groupBy({ it.first }, { it.second }).entries.forEach { (packageName, services) ->
            generate(packageName, GENERATED_PROXY) {
                services.sortedBy { it.name() }.forEach { generateProxy(it) }
            }
        }

        return emptyList() // TODO: validate
    }
}

public class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = YassProcessor(environment)
}

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
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
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import kotlin.reflect.KClass

internal fun KSDeclaration.qualifiedName() = qualifiedName!!.asString()
internal val KSDeclaration.name get() = simpleName.asString()
internal val KSType.qualifiedName get() = declaration.qualifiedName()

internal fun KSClassDeclaration.getAllPropertiesNotThrowable(): List<KSPropertyDeclaration> {
    fun KSPropertyDeclaration.isPropertyOfThrowable() =
        (name == "cause" || name == "message") && ("kotlin.Throwable" == parentDeclaration!!.qualifiedName())
    return getAllProperties()
        .toList()
        .filterNot { it.isPropertyOfThrowable() }
        .sortedBy { it.name }
}

internal fun KSTypeReference.type(): String {
    fun Appendable.appendGenerics() {
        val element = element ?: error("generic type '${this@type}' must not be implicit @${parent?.location}")
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

internal fun List<KSType>.getBaseEncoderTypes() =
    map { (it.declaration as KSClassDeclaration).superTypes.first().element!!.typeArguments.first().type!!.resolve() }

// NOTE: default values in annotations don't yet work for multiplatform libraries;
//       see https://youtrack.jetbrains.com/issue/KT-59526/Store-annotation-default-values-in-metadata-on-JVM
private fun KSAnnotation.argument(name: String) = arguments.first { it.name!!.asString() == name }.value!!

private fun KSType.isEnum() = (declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS

private fun KSAnnotation.checkClasses(classes: List<KSType>, enumMessage: String) {
    require(classes.size == classes.toSet().size) { "class must not be duplicated @$location" }
    classes.firstOrNull { it.isEnum() }?.let { klass -> error("enum class '${klass.qualifiedName}' $enumMessage @$location") }
}

private fun KSDeclaration.firstOrNull(annotation: KClass<*>) =
    annotations.firstOrNull { it.shortName.asString() == annotation.simpleName }

private class Yass2Processor(environment: SymbolProcessorEnvironment) : SymbolProcessor { // TODO: review
    private val codeGenerator = environment.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val packageName2declarations = buildList {
            listOf(GenerateProxy::class, GenerateBinarySerializer::class, GenerateDumper::class).forEach { annotation ->
                resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!)
                    .map { it as KSDeclaration }
                    .forEach { declaration -> add(Pair(declaration.packageName.asString(), declaration)) }
            }
        }.groupBy({ it.first }, { it.second })
        packageName2declarations.forEach { (packageName, declarations) ->
            codeGenerator.createNewFile(
                Dependencies.ALL_FILES, // we want to be on the safe side
                packageName,
                GENERATED_BY_YASS,
            ).writer().use { writer ->
                writer.appendPackage(packageName)
                val codeWriter = CodeWriter(writer)

                buildList {
                    declarations.forEach { declaration ->
                        declaration.firstOrNull(GenerateProxy::class)?.let { add(declaration) }
                    }
                }.sortedBy { it.name }.forEach { codeWriter.generateProxy(it as KSClassDeclaration) }

                fun List<KSDeclaration>.annotations(annotation: KClass<*>): KSAnnotation? {
                    val annotations = buildList {
                        this@annotations.forEach { declaration ->
                            declaration.firstOrNull(annotation)?.let { add(it) }
                        }
                    }
                    require(annotations.size <= 1) {
                        "there can be at most one annotation '${annotation.simpleName}' in package '$packageName'"
                    }
                    return annotations.firstOrNull()
                }

                val serializer = declarations.annotations(GenerateBinarySerializer::class)
                val dumper = declarations.annotations(GenerateDumper::class)
                require((dumper == null) || (serializer == null) || !(serializer.argument("withDumper") as Boolean)) {
                    "illegal use of annotations '${GenerateBinarySerializer::class.simpleName}' '${GenerateDumper::class.simpleName}' in package '$packageName'"
                }

                @Suppress("UNCHECKED_CAST")
                if (serializer != null) {
                    val baseEncoderClasses = serializer.argument("baseEncoderClasses") as List<KSType>
                    val enumClasses = serializer.argument("enumClasses") as List<KSType>
                    val treeConcreteClasses = serializer.argument("treeConcreteClasses") as List<KSType>
                    val graphConcreteClasses = serializer.argument("graphConcreteClasses") as List<KSType>
                    val withDumper = serializer.argument("withDumper") as Boolean
                    require(enumClasses.size == enumClasses.toSet().size) { "enum classes must not be duplicated @${serializer.location}" }
                    enumClasses.forEach {
                        require(it.isEnum()) { "class '${it.qualifiedName}' in enumClasses must be enum @${serializer.location}" }
                    }
                    serializer.checkClasses(
                        baseEncoderClasses.getBaseEncoderTypes() + treeConcreteClasses + graphConcreteClasses,
                        "belongs to 'enumClasses'"
                    )
                    codeWriter.generateBinarySerializer(baseEncoderClasses, enumClasses, treeConcreteClasses, graphConcreteClasses)
                    if (withDumper) codeWriter.generateDumper(treeConcreteClasses, graphConcreteClasses)
                }

                @Suppress("UNCHECKED_CAST")
                if (dumper != null) {
                    val treeConcreteClasses = dumper.argument("treeConcreteClasses") as List<KSType>
                    val graphConcreteClasses = dumper.argument("graphConcreteClasses") as List<KSType>
                    dumper.checkClasses(treeConcreteClasses + graphConcreteClasses, "must not be specified")
                    codeWriter.generateDumper(treeConcreteClasses, graphConcreteClasses)
                }
            }
        }
        return emptyList()
    }
}

public class Yass2Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = Yass2Processor(environment)
}

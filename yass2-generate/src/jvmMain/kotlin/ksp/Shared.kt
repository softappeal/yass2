package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.*
import ch.softappeal.yass2.generate.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

// TODO: implement KSP for BinarySerializer and DumperProperties

internal fun KSClassDeclaration.name() = qualifiedName!!.asString()

@Suppress("SpellCheckingInspection")
public const val TEST_PACKAGE: String = "ksptest" // TODO: remove if switched to KSP

public class YassProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false // TODO: is this really needed?
    private val codeGenerator = environment.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        invoked = true

        fun generate(packageName: String, file: String, generate: Appendable.() -> Unit) {
            codeGenerator.createNewFile(Dependencies(false), "$packageName.$TEST_PACKAGE", file).writer().use { writer ->
                writer.writeHeader("$packageName.$TEST_PACKAGE")
                writer.generate()
            }
        }

        buildMap {
            resolver.getAllFiles().forEach { file ->
                file.annotations.forEach { annotation ->
                    if (annotation.shortName.asString() == GenerateBinarySerializer::class.simpleName) {
                        val packageName = file.packageName.asString()
                        check(put(packageName, annotation) == null) {
                            "duplicated annotation ${GenerateBinarySerializer::class.qualifiedName} in package $packageName: ${annotation.location}"
                        }
                    }
                }
            }
        }.entries.forEach { (packageName, annotation) ->
            generate(packageName, GENERATED_BINARY_SERIALIZER) { generateBinarySerializer(annotation) }
        }

        val unitType = resolver.builtIns.unitType
        buildList {
            resolver.getSymbolsWithAnnotation(Proxy::class.qualifiedName!!)
                .forEach { add(Pair((it as KSClassDeclaration).packageName.asString(), it)) }
        }.groupBy({ it.first }, { it.second }).entries.forEach { (packageName, services) ->
            generate(packageName, GENERATED_PROXY) {
                services.sortedBy { it.name() }.forEach { generateProxy(it, unitType) }
            }
        }

        return emptyList()
    }
}

public class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = YassProcessor(environment)
}

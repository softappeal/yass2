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
    private val codeGenerator = environment.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Proxy::class.qualifiedName!!)
        val packageToServices = buildList { symbols.forEach { add(Pair((it as KSClassDeclaration).packageName.asString(), it)) } }.groupBy({ it.first }, { it.second })
        val unitType = resolver.builtIns.unitType
        packageToServices.entries.forEach { (packageName, services) ->
            codeGenerator.createNewFile(Dependencies(false), "$packageName.$TEST_PACKAGE", GENERATED_PROXY).writer().use { writer ->
                writer.writeHeader("$packageName.$TEST_PACKAGE")
                services.sortedBy { it.name() }.forEach { writer.generateProxy(it, unitType) }
            }
        }
        return emptyList()
    }
}

public class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = YassProcessor(environment)
}

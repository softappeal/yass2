package ch.softappeal.yass2.ksp // TODO

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

public class YassProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false
    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        invoked = true
        logger.warn("----------------")
        resolver.getAllFiles().forEach { file ->
            file.annotations.forEach { annotation ->
                val packageName = "${file.packageName.getQualifier()}.${file.packageName.getShortName()}"
                codeGenerator.createNewFile(
                    Dependencies(false),
                    packageName,
                    "GeneratedFunction"
                ).writer().use { out ->
                    out.appendLine("package $packageName")
                    out.appendLine("public const val TEST_VALUE: Int = 123")
                    annotation.arguments.forEach { argument ->
                        logger.warn(argument.name!!.getShortName())
                        @Suppress("UNCHECKED_CAST") val list = argument.value as List<KSAnnotation>
                        list.forEach { serviceWithId ->
                            logger.warn("  ${serviceWithId.shortName.getShortName()}")
                            serviceWithId.arguments.forEach { argument ->
                                val value = argument.value!!
                                logger.warn("    ${argument.name!!.getShortName()} $value '${value::class}'")
                                if (value is KSType) {
                                    logger.warn("      " + value.declaration.qualifiedName!!.asString())
                                }
                            }
                        }
                    }
                }
            }
        }
        return emptyList()
    }
}

public class YassProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = YassProcessor(environment)
}

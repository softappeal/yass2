package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

internal fun CodeWriter.generateDumper(
    treeConcreteClasses: List<KSType>,
    graphConcreteClasses: List<KSType>,
    declaration: KSPropertyDeclaration,
) {
    val classes = treeConcreteClasses + graphConcreteClasses
    (treeConcreteClasses + graphConcreteClasses).checkNotDuplicated(declaration)
    checkNotEnum(declaration, classes, "must not be specified")

    writeLine()
    writeNestedLine("public ${declaration.actual()}fun createDumper(dumpValue: $CSY.ValueDumper): $CSY.Dumper =") {
        writeNestedLine("$CSY.createDumper(") {
            writeNestedLine("$CSY.dumperProperties(") {
                (treeConcreteClasses + graphConcreteClasses).forEach { type ->
                    writeNestedLine("${type.qualifiedName}::class to listOf(") {
                        (type.declaration as KSClassDeclaration).getAllPropertiesNotThrowable().forEach { property ->
                            writeNestedLine("${type.qualifiedName}::${property.name} as kotlin.reflect.KProperty1<Any, Any?>,")
                        }
                    }
                    writeNestedLine("),")
                }
            }
            writeNestedLine("),")
            writeNestedLine("setOf(") {
                graphConcreteClasses.forEach { type -> writeNestedLine("${type.qualifiedName}::class,") }
            }
            writeNestedLine("),")
            writeNestedLine("dumpValue,")
        }
        writeNestedLine(")")
    }
}

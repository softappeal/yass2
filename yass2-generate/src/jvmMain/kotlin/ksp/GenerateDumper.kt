package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

internal fun CodeWriter.generateDumper(
    treeConcreteClasses: List<KSType>,
    graphConcreteClasses: List<KSType>,
) {
    val classes = treeConcreteClasses + graphConcreteClasses
    require(classes.size == classes.toSet().size) { "class must not be duplicated" }
    checkNotEnum(classes, "must not be specified")

    writeLine()
    writeNestedLine("public fun createDumper(dumpValue: kotlin.text.Appendable.(value: kotlin.Any) -> kotlin.Unit): $CSY.Dumper =") {
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

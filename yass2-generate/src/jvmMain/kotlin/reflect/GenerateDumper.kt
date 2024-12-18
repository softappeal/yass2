package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
import kotlin.reflect.KClass

public fun CodeWriter.generateDumper(
    treeConcreteClasses: List<KClass<*>>,
    graphConcreteClasses: List<KClass<*>> = emptyList(),
) {
    val classes = treeConcreteClasses + graphConcreteClasses
    classes.checkNotDuplicated()
    checkNotEnum(classes, "must not be specified")

    writeLine()
    writeNestedLine("public fun createDumper(dumpValue: $CSY.ValueDumper): $CSY.Dumper =") {
        writeNestedLine("$CSY.createDumper(") {
            writeNestedLine("$CSY.dumperProperties(") {
                (treeConcreteClasses + graphConcreteClasses).forEach { type ->
                    writeNestedLine("${type.qualifiedName}::class to listOf(") {
                        type.getAllPropertiesNotThrowable().forEach { property ->
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

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.appendLine
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

internal fun Appendable.generateDumper(treeConcreteClasses: List<KSType>, graphConcreteClasses: List<KSType>) {
    appendLine()
    appendLine("public fun createDumper(dumpValue: kotlin.text.Appendable.(value: kotlin.Any) -> kotlin.Unit): $CSY.Dumper =")
    appendLine(1, "$CSY.createDumper(")
    appendLine(2, "$CSY.dumperProperties(")
    (treeConcreteClasses + graphConcreteClasses).forEach { type ->
        appendLine(3, "${type.qualifiedName}::class to listOf(")
        (type.declaration as KSClassDeclaration).getAllPropertiesNotThrowable().forEach { property ->
            appendLine(4, "${type.qualifiedName}::${property.name} as kotlin.reflect.KProperty1<Any, Any?>,")
        }
        appendLine(3, "),")
    }
    appendLine(2, "),")
    appendLine(2, "setOf(")
    graphConcreteClasses.forEach { type -> appendLine(3, "${type.qualifiedName}::class,") }
    appendLine(2, "),")
    appendLine(2, "dumpValue,")
    appendLine(1, ")")
}

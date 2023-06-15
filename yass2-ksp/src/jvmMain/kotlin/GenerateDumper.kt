package ch.softappeal.yass2.ksp

import com.google.devtools.ksp.symbol.*

internal fun Appendable.generateDumper(treeConcreteClasses: List<KSType>, graphConcreteClasses: List<KSType>) {
    appendLine()
    appendLine("public fun createDumper(dumpValue: kotlin.text.Appendable.(value: kotlin.Any) -> kotlin.Unit): $CSY.Dumper = $CSY.createDumper(")
    appendLine("    $CSY.dumperProperties(")
    (treeConcreteClasses + graphConcreteClasses).forEach { type ->
        appendLine(2, "${type.qualifiedName()}::class to listOf(")
        (type.declaration as KSClassDeclaration).getAllPropertiesNotThrowable().forEach { property ->
            appendLine(3, "${type.qualifiedName()}::${property.simpleName()} as kotlin.reflect.KProperty1<Any, Any?>,")
        }
        appendLine(2, "),")
    }
    appendLine("    ),")
    appendLine("    setOf(")
    graphConcreteClasses.forEach { type -> appendLine(2, "${type.qualifiedName()}::class,") }
    appendLine("    ),")
    appendLine("    dumpValue,")
    appendLine(")")
}

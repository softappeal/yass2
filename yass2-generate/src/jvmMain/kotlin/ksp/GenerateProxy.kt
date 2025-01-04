@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.sortMethods
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

private val AnyFunctions = setOf("toString", "equals", "hashCode")
private val KSFunctionDeclaration.isSuspend get() = Modifier.SUSPEND in modifiers
private fun KSFunctionDeclaration.hasResult() = "kotlin.Unit" != returnType!!.resolve().qualifiedName

private fun CodeWriter.writeSignature(function: KSFunctionDeclaration) {
    writeNestedLine("override ${if (function.isSuspend) "suspend " else ""}fun ${function.name}(") {
        function.parameters.forEachIndexed { parameterIndex, parameter ->
            writeNestedLine("p${parameterIndex + 1}: ${parameter.type.type()},")
        }
    }
    writeNested(")")
}

private fun KSFunctionDeclaration.parameters() = (1..parameters.size).joinToString(", ") { "p$it" }

private val KSClassDeclaration.withTypeParameters get() = "<${typeParameters.joinToString { it.simpleName.asString() }}>"
private val KSClassDeclaration.withTypes get() = "${qualifiedName()}${if (typeParameters.isEmpty()) "" else withTypeParameters}"
private val KSClassDeclaration.types get() = if (typeParameters.isEmpty()) "" else " $withTypeParameters"

internal fun CodeWriter.generateProxy(service: KSClassDeclaration) {
    require(service.classKind == ClassKind.INTERFACE) { "${service.qualifiedName()} must be an interface @${service.location}" }

    val functions = service.getAllFunctions().toList()
        .filter { it.name !in AnyFunctions }
        .sortMethods({ name }, { service.qualifiedName() }, " @${service.location}")

    writeLine()
    writeNestedLine("public ${service.actual()}fun${service.types} ${service.withTypes}.proxy(") {
        if (functions.any { !it.isSuspend }) writeNestedLine("intercept: $CSY.Interceptor,")
        if (functions.any { it.isSuspend }) writeNestedLine("suspendIntercept: $CSY.SuspendInterceptor,")
    }
    writeNestedLine("): ${service.withTypes} = object : ${service.withTypes} {", "}") {
        functions.forEachIndexed { functionIndex, function ->
            if (functionIndex != 0) writeLine()
            val hasResult = function.hasResult()
            writeSignature(function)
            if (hasResult) write(": ${function.returnType!!.type()}")
            writeLine(" {") {
                writeNestedLine("${if (hasResult) "return " else ""}${if (function.isSuspend) "suspendIntercept" else "intercept"}(${service.withTypes}::${function.name}, listOf(${function.parameters()})) {") {
                    writeNestedLine("this@proxy.${function.name}(${function.parameters()})")
                }
                writeNested("}")
            }
            if (hasResult) write(" as ${function.returnType!!.type()}")
            writeLine()
            writeNestedLine("}")
        }
    }

    if (functions.any { !it.isSuspend }) return

    writeLine()
    writeNestedLine("public ${service.actual()}fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.proxy(") {
        writeNestedLine("tunnel: $CSY.remote.Tunnel,")
    }
    writeNestedLine("): ${service.withTypes} =") {
        writeNestedLine("object : ${service.withTypes} {", "}") {
            functions.forEachIndexed { functionIndex, function ->
                if (functionIndex != 0) writeLine()
                val hasResult = function.hasResult()
                writeSignature(function)
                writeLine(" ${if (hasResult) "=" else "{"}") {
                    writeNestedLine("tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(${function.parameters()})))") {
                        writeNested(".process()")
                        if (hasResult) write(" as ${function.returnType!!.type()}") else writeLine()
                    }
                }
                if (!hasResult) writeNested("}")
                writeLine()
            }
        }
    }

    writeLine()
    writeNestedLine("public ${service.actual()}fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.service(") {
        writeNestedLine("implementation: ${service.withTypes},")
    }
    writeNestedLine("): ${Service::class.qualifiedName} =") {
        writeNestedLine("${Service::class.qualifiedName}(id) { functionId, parameters ->", "}") {
            writeNestedLine("when (functionId) {", "}") {
                functions.forEachIndexed { functionIndex, function ->
                    writeNestedLine("$functionIndex -> implementation.${function.name}(", ")") {
                        function.parameters.forEachIndexed { parameterIndex, parameter ->
                            writeNestedLine("parameters[$parameterIndex] as ${parameter.type.type()},")
                        }
                    }
                }
                writeNestedLine("else -> error(\"service with id ${'$'}id has no function with id ${'$'}functionId\")")
            }
        }
    }
}

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
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

internal fun CodeWriter.generateProxy(service: KSClassDeclaration) {
    require(service.classKind == ClassKind.INTERFACE) { "'${service.qualifiedName()}' must be an interface @${service.location}" }

    val functions = service.getAllFunctions()
        .toList()
        .filter { it.name !in AnyFunctions }
        .sortedBy { it.name } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
        .apply {
            require(map { it.name }.toSet().size == size) {
                "interface '${service.qualifiedName()}' must not overload functions @${service.location}"
            }
        }

    writeLine()
    writeNestedLine("public fun ${service.qualifiedName()}.proxy(") {
        if (functions.any { !it.isSuspend }) writeNestedLine("intercept: $CSY.Interceptor,")
        if (functions.any { it.isSuspend }) writeNestedLine("suspendIntercept: $CSY.SuspendInterceptor,")
    }
    writeNestedLine("): ${service.qualifiedName()} = object : ${service.qualifiedName()} {") {
        functions.forEachIndexed { functionIndex, function ->
            if (functionIndex != 0) writeLine()
            val hasResult = function.hasResult()
            writeSignature(function)
            if (hasResult) write(": ${function.returnType!!.type()}")
            writeLine(" {") {
                writeNestedLine("${if (hasResult) "return " else ""}${if (function.isSuspend) "suspendIntercept" else "intercept"}(${service.qualifiedName()}::${function.name}, listOf(${function.parameters()})) {") {
                    writeNestedLine("this@proxy.${function.name}(${function.parameters()})")
                }
                writeNested("}")
            }
            if (hasResult) write(" as ${function.returnType!!.type()}")
            writeLine()
            writeNestedLine("}")
        }
    }
    writeNestedLine("}")

    if (functions.any { !it.isSuspend }) return

    writeLine()
    writeNestedLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName()}>.proxy(") {
        writeNestedLine("tunnel: $CSY.remote.Tunnel,")
    }
    writeNestedLine("): ${service.qualifiedName()} =") {
        writeNestedLine("object : ${service.qualifiedName()} {") {
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
        writeNestedLine("}")
    }

    writeLine()
    writeNestedLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName()}>.service(") {
        writeNestedLine("implementation: ${service.qualifiedName()},")
    }
    writeNestedLine("): ${Service::class.qualifiedName} =") {
        writeNestedLine("${Service::class.qualifiedName}(id) { functionId, parameters ->") {
            writeNestedLine("when (functionId) {") {
                functions.forEachIndexed { functionIndex, function ->
                    writeNestedLine("$functionIndex -> implementation.${function.name}(") {
                        function.parameters.forEachIndexed { parameterIndex, parameter ->
                            writeNestedLine("parameters[$parameterIndex] as ${parameter.type.type()},")
                        }
                    }
                    writeNestedLine(")")
                }
                writeNestedLine("else -> error(\"service with id ${'$'}id has no function with id ${'$'}functionId\")")
            }
            writeNestedLine("}")
        }
        writeNestedLine("}")
    }
}

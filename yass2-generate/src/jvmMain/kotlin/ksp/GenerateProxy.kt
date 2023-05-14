package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.remote.*
import com.google.devtools.ksp.symbol.*

private val AnyFunctions = setOf("toString", "equals", "hashCode")

private fun KSClassDeclaration.serviceFunctions() = getAllFunctions()
    .filter { !AnyFunctions.contains(it.toString()) }
    .toList()
    .sortedBy { it.toString() } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
    .apply {
        require(map { it.toString() }.toSet().size == size) { "'${this@serviceFunctions.name()}' has overloaded functions" }
    }

private fun KSFunctionDeclaration.isSuspend() = modifiers.contains(Modifier.SUSPEND)

private fun KSTypeReference.name(): String {
    val type = resolve()
    return "${type.declaration.packageName.asString()}.${toString()}${if (type.isMarkedNullable) "?" else ""}"
}

internal fun Appendable.generateProxy(service: KSClassDeclaration, unitType: KSType) {
    generateLocalProxy(service, unitType)
    if (service.serviceFunctions().all { it.isSuspend() }) {
        generateRemoteProxy(service, unitType)
        generateService(service)
    }
}

private fun KSFunctionDeclaration.hasResult(unitType: KSType) = unitType != returnType!!.resolve()

private fun Appendable.writeFunctionSignature(indent: String, function: KSFunctionDeclaration) {
    append("${indent}override ${if (function.isSuspend()) "suspend " else ""}fun $function(")
    function.parameters.forEachIndexed { parameterIndex, parameter ->
        if (parameterIndex != 0) append(", ")
        append("p${parameterIndex + 1}: ${parameter.type.name()}")
    }
    append(")")
}

private fun Appendable.parameterList(function: KSFunctionDeclaration) {
    for (parameterIndex in 1..function.parameters.size) {
        if (parameterIndex != 1) append(", ")
        append("p$parameterIndex")
    }
}

private fun Appendable.generateLocalProxy(service: KSClassDeclaration, unitType: KSType) {
    write("""

        public fun ${service.name()}.proxy(
    """)
    val functions = service.serviceFunctions()
    if (functions.any { !it.isSuspend() }) appendLine("    intercept: $CSY.Interceptor,")
    if (functions.any { it.isSuspend() }) appendLine("    suspendIntercept: $CSY.SuspendInterceptor,")
    write("""
        ): ${service.name()} = object : ${service.name()} {
    """)
    functions.forEachIndexed { functionIndex, function ->
        val hasResult = function.hasResult(unitType)
        if (functionIndex != 0) appendLine()
        writeFunctionSignature("    ", function)
        if (hasResult) append(": ${function.returnType!!.name()}")
        appendLine(" {")
        append("        ${if (hasResult) "return " else ""}${if (function.isSuspend()) "suspendIntercept" else "intercept"}(${service.name()}::$function, ")
        append("listOf(")
        parameterList(function)
        append(")) { this@proxy.$function(")
        parameterList(function)
        append(") }")
        if (hasResult) append(" as ${function.returnType!!.name()}")
        appendLine()
        appendLine("    }")
    }
    write("""
        }
    """)
}

private fun Appendable.generateRemoteProxy(service: KSClassDeclaration, unitType: KSType) {
    write("""

        public fun ${ServiceId::class.qualifiedName}<${service.name()}>.proxy(tunnel: $CSY.remote.Tunnel): ${service.name()} =
            object : ${service.name()} {
    """)
    service.serviceFunctions().forEachIndexed { functionIndex, function ->
        val hasResult = function.hasResult(unitType)
        if (functionIndex != 0) appendLine()
        writeFunctionSignature("        ", function)
        if (hasResult) append(" = ") else append(" {\n            ")
        append("tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(")
        parameterList(function)
        append("))).process()")
        if (hasResult) append(" as ${function.returnType!!.name()}") else append("\n        }")
        appendLine()
    }
    write("""
        }
    """, 1)
}

private fun Appendable.generateService(service: KSClassDeclaration) {
    write("""

        public fun ${ServiceId::class.qualifiedName}<${service.name()}>.service(implementation: ${service.name()}): ${Service::class.qualifiedName} =
            ${Service::class.qualifiedName}(id) { functionId, parameters ->
                when (functionId) {
    """)
    service.serviceFunctions().forEachIndexed { functionIndex, function ->
        append("            $functionIndex -> implementation.$function(")
        function.parameters.forEachIndexed { parameterIndex, parameter ->
            if (parameterIndex != 0) append(", ")
            append("parameters[$parameterIndex] as ${parameter.type.name()}")
        }
        appendLine(")")
    }
    write("""
                else -> error("service with id ${'$'}id has no function with id ${'$'}functionId")
            }
        }
    """, 1)
}

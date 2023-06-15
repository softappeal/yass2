package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.remote.*
import com.google.devtools.ksp.symbol.*

private val AnyFunctions = setOf("toString", "equals", "hashCode")
private fun KSFunctionDeclaration.isSuspend() = Modifier.SUSPEND in modifiers
private fun KSFunctionDeclaration.hasResult() = "kotlin.Unit" != returnType!!.resolve().qualifiedName()

internal fun Appendable.generateProxy(service: KSClassDeclaration) {
    require(service.classKind == ClassKind.INTERFACE) { "'${service.qualifiedName()}' must be an interface @${service.location}" }

    val functions = service.getAllFunctions()
        .toList()
        .filter { it.simpleName() !in AnyFunctions }
        .sortedBy { it.simpleName() } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
        .apply {
            require(map { it.simpleName() }.toSet().size == size) { "interface '${service.qualifiedName()}' must not overload functions @${service.location}" }
        }

    fun appendSignature(level: Int, function: KSFunctionDeclaration) {
        append(level, "override ${if (function.isSuspend()) "suspend " else ""}fun ${function.simpleName()}(")
        function.parameters.forEachIndexed { parameterIndex, parameter ->
            if (parameterIndex != 0) append(", ")
            append("p${parameterIndex + 1}: ").appendType(parameter.type)
        }
        append(')')
    }

    fun Appendable.appendParameters(function: KSFunctionDeclaration): Appendable {
        for (parameterIndex in 1..function.parameters.size) {
            if (parameterIndex != 1) append(", ")
            append("p$parameterIndex")
        }
        return this
    }

    appendLine()
    appendLine("public fun ${service.qualifiedName()}.proxy(")
    if (functions.any { !it.isSuspend() }) appendLine(1, "intercept: $CSY.Interceptor,")
    if (functions.any { it.isSuspend() }) appendLine(1, "suspendIntercept: $CSY.SuspendInterceptor,")
    appendLine("): ${service.qualifiedName()} = object : ${service.qualifiedName()} {")
    functions.forEachIndexed { functionIndex, function ->
        if (functionIndex != 0) appendLine()
        val hasResult = function.hasResult()
        appendSignature(1, function)
        if (hasResult) append(": ").appendType(function.returnType!!)
        appendLine(" {")
        append(2, "${if (hasResult) "return " else ""}${if (function.isSuspend()) "suspendIntercept" else "intercept"}(${service.qualifiedName()}::${function.simpleName()}, ")
        append("listOf(").appendParameters(function).append(")) { this@proxy.${function.simpleName()}(").appendParameters(function).append(") }")
        if (hasResult) append(" as ").appendType(function.returnType!!)
        appendLine()
        appendLine(1, "}")
    }
    appendLine("}")

    if (functions.any { !it.isSuspend() }) return

    appendLine()
    appendLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName()}>.proxy(tunnel: $CSY.remote.Tunnel): ${service.qualifiedName()} =")
    appendLine("    object : ${service.qualifiedName()} {")
    functions.forEachIndexed { functionIndex, function ->
        if (functionIndex != 0) appendLine()
        val hasResult = function.hasResult()
        appendSignature(2, function)
        if (hasResult) append(" =") else append(" {")
        appendLine()
        append(3, "tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(").appendParameters(function).append("))).process()")
        if (hasResult) append(" as ").appendType(function.returnType!!) else appendLine().append(2, "}")
        appendLine()
    }
    appendLine("    }")

    appendLine()
    appendLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName()}>.service(implementation: ${service.qualifiedName()}): ${Service::class.qualifiedName} =")
    appendLine("    ${Service::class.qualifiedName}(id) { functionId, parameters ->")
    appendLine("        when (functionId) {")
    functions.forEachIndexed { functionIndex, function ->
        append(3, "$functionIndex -> implementation.${function.simpleName()}(")
        function.parameters.forEachIndexed { parameterIndex, parameter ->
            if (parameterIndex != 0) append(", ")
            append("parameters[$parameterIndex] as ").appendType(parameter.type)
        }
        appendLine(')')
    }
    appendLine("            else -> error(\"service with id ${'$'}id has no function with id ${'$'}functionId\")")
    appendLine("        }")
    appendLine("    }")
}

package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

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
            require(map { it.simpleName() }.toSet().size == size) {
                "interface '${service.qualifiedName()}' must not overload functions @${service.location}"
            }
        }

    fun appendSignature(level: Int, function: KSFunctionDeclaration) {
        appendLine(level, "override ${if (function.isSuspend()) "suspend " else ""}fun ${function.simpleName()}(")
        function.parameters.forEachIndexed { parameterIndex, parameter ->
            append(level + 1, "p${parameterIndex + 1}: ").appendType(parameter.type).appendLine(',')
        }
        append(level, ")")
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
        append(
            2,
            "${if (hasResult) "return " else ""}${if (function.isSuspend()) "suspendIntercept" else "intercept"}" +
                "(${service.qualifiedName()}::${function.simpleName()}, listOf("
        ).appendParameters(function).appendLine(")) {")
        append(3, "this@proxy.${function.simpleName()}(").appendParameters(function).append(')').appendLine()
        append(2, "}")
        if (hasResult) append(" as ").appendType(function.returnType!!)
        appendLine()
        appendLine(1, "}")
    }
    appendLine("}")

    if (functions.any { !it.isSuspend() }) return

    appendLine()
    appendLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName()}>.proxy(")
    appendLine(1, "tunnel: $CSY.remote.Tunnel,")
    appendLine("): ${service.qualifiedName()} =")
    appendLine(1, "object : ${service.qualifiedName()} {")
    functions.forEachIndexed { functionIndex, function ->
        if (functionIndex != 0) appendLine()
        val hasResult = function.hasResult()
        appendSignature(2, function)
        if (hasResult) append(" =") else append(" {")
        appendLine()
        append(3, "tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(")
            .appendParameters(function).append(")))").appendLine()
        append(4, ".process()")
        if (hasResult) append(" as ").appendType(function.returnType!!) else appendLine().append(2, "}")
        appendLine()
    }
    appendLine(1, "}")

    appendLine()
    appendLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName()}>.service(")
    appendLine(1, "implementation: ${service.qualifiedName()},")
    appendLine("): ${Service::class.qualifiedName} =")
    appendLine(1, "${Service::class.qualifiedName}(id) { functionId, parameters ->")
    appendLine(2, "when (functionId) {")
    functions.forEachIndexed { functionIndex, function ->
        appendLine(3, "$functionIndex -> implementation.${function.simpleName()}(")
        function.parameters.forEachIndexed { parameterIndex, parameter ->
            append(4, "parameters[$parameterIndex] as ").appendType(parameter.type).appendLine(',')
        }
        appendLine(3, ")")
    }
    appendLine(3, "else -> error(\"service with id ${'$'}id has no function with id ${'$'}functionId\")")
    appendLine(2, "}")
    appendLine(1, "}")
}

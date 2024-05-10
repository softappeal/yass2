package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.append
import ch.softappeal.yass2.generate.appendLine
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

private fun KFunction<*>.hasResult() = returnType.classifier != Unit::class

internal fun Appendable.generateProxy(service: KClass<*>) {
    // TODO require(service.classKind == ClassKind.INTERFACE) { "'${service.qualifiedName()}' must be an interface @${service.location}" }

    val functions = service.memberFunctions
        .filter { it.javaMethod!!.declaringClass != Object::class.java }
        .sortedBy(KFunction<*>::name) // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
        .apply {
            require(map(KFunction<*>::name).toSet().size == size) {
                "'$service' has overloaded functions"
            }
        }

    fun appendSignature(level: Int, function: KFunction<*>) {
        appendLine(level, "override ${if (function.isSuspend) "suspend " else ""}fun ${function.name}(")
        function.valueParameters.forEachIndexed { parameterIndex, parameter ->
            appendLine(level + 1, "p${parameterIndex + 1}: ${parameter.type},")
        }
        append(level, ")")
    }

    fun Appendable.appendParameters(function: KFunction<*>): Appendable {
        for (parameterIndex in 1..function.valueParameters.size) {
            if (parameterIndex != 1) append(", ")
            append("p$parameterIndex")
        }
        return this
    }

    appendLine()
    appendLine("public fun ${service.qualifiedName}.proxy(")
    if (functions.any { !it.isSuspend }) appendLine(1, "intercept: $CSY.Interceptor,")
    if (functions.any { it.isSuspend }) appendLine(1, "suspendIntercept: $CSY.SuspendInterceptor,")
    appendLine("): ${service.qualifiedName} = object : ${service.qualifiedName} {")
    functions.forEachIndexed { functionIndex, function ->
        if (functionIndex != 0) appendLine()
        val hasResult = function.hasResult()
        appendSignature(1, function)
        if (hasResult) append(": ${function.returnType}")
        appendLine(" {")
        append(
            2,
            "${if (hasResult) "return " else ""}${if (function.isSuspend) "suspendIntercept" else "intercept"}" +
                "(${service.qualifiedName}::${function.name}, listOf("
        ).appendParameters(function).appendLine(")) {")
        append(3, "this@proxy.${function.name}(").appendParameters(function).append(')').appendLine()
        append(2, "}")
        if (hasResult) append(" as ${function.returnType}")
        appendLine()
        appendLine(1, "}")
    }
    appendLine("}")

    if (functions.any { !it.isSuspend }) return

    appendLine()
    appendLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName}>.proxy(")
    appendLine(1, "tunnel: $CSY.remote.Tunnel,")
    appendLine("): ${service.qualifiedName} =")
    appendLine(1, "object : ${service.qualifiedName} {")
    functions.forEachIndexed { functionIndex, function ->
        if (functionIndex != 0) appendLine()
        val hasResult = function.hasResult()
        appendSignature(2, function)
        if (hasResult) append(" =") else append(" {")
        appendLine()
        append(3, "tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(")
            .appendParameters(function).append(")))").appendLine()
        append(4, ".process()")
        if (hasResult) append(" as ${function.returnType}") else appendLine().append(2, "}")
        appendLine()
    }
    appendLine(1, "}")

    appendLine()
    appendLine("public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName}>.service(")
    appendLine(1, "implementation: ${service.qualifiedName},")
    appendLine("): ${Service::class.qualifiedName} =")
    appendLine(1, "${Service::class.qualifiedName}(id) { functionId, parameters ->")
    appendLine(2, "when (functionId) {")
    functions.forEachIndexed { functionIndex, function ->
        appendLine(3, "$functionIndex -> implementation.${function.name}(")
        function.valueParameters.forEachIndexed { parameterIndex, parameter ->
            appendLine(4, "parameters[$parameterIndex] as ${parameter.type},")
        }
        appendLine(3, ")")
    }
    appendLine(3, "else -> error(\"service with id ${'$'}id has no function with id ${'$'}functionId\")")
    appendLine(2, "}")
    appendLine(1, "}")
}

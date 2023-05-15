package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.remote.*
import com.google.devtools.ksp.symbol.*

private val AnyFunctions = setOf("toString", "equals", "hashCode")

private fun KSFunctionDeclaration.isSuspend() = modifiers.contains(Modifier.SUSPEND)

private fun KSTypeReference.name(): String {
    val type = resolve()
    return "${type.declaration.packageName.asString()}.${toString()}${if (type.isMarkedNullable) "?" else ""}"
}

internal fun Appendable.generateProxy(service: KSClassDeclaration, unitType: KSType) {
    val functions = service.getAllFunctions()
        .filter { !AnyFunctions.contains(it.toString()) }
        .toList()
        .sortedBy { it.toString() } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
        .apply {
            require(map { it.toString() }.toSet().size == size) { "'${service.name()}' has overloaded functions" }
        }

    fun KSFunctionDeclaration.hasResult() = unitType != returnType!!.resolve()

    fun KSFunctionDeclaration.signature(indent: String) {
        append("${indent}override ${if (isSuspend()) "suspend " else ""}fun $this(")
        parameters.forEachIndexed { parameterIndex, parameter ->
            if (parameterIndex != 0) append(", ")
            append("p${parameterIndex + 1}: ${parameter.type.name()}")
        }
        append(")")
    }

    fun KSFunctionDeclaration.parameters() {
        for (parameterIndex in 1..parameters.size) {
            if (parameterIndex != 1) append(", ")
            append("p$parameterIndex")
        }
    }

    write("""

        public fun ${service.name()}.proxy(
    """)
    if (functions.any { !it.isSuspend() }) appendLine("    intercept: $CSY.Interceptor,")
    if (functions.any { it.isSuspend() }) appendLine("    suspendIntercept: $CSY.SuspendInterceptor,")
    write("""
        ): ${service.name()} = object : ${service.name()} {
    """)
    functions.forEachIndexed { functionIndex, function ->
        val hasResult = function.hasResult()
        if (functionIndex != 0) appendLine()
        function.signature("    ")
        if (hasResult) append(": ${function.returnType!!.name()}")
        appendLine(" {")
        append("        ${if (hasResult) "return " else ""}${if (function.isSuspend()) "suspendIntercept" else "intercept"}(${service.name()}::$function, ")
        append("listOf(")
        function.parameters()
        append(")) { this@proxy.$function(")
        function.parameters()
        append(") }")
        if (hasResult) append(" as ${function.returnType!!.name()}")
        appendLine()
        appendLine("    }")
    }
    write("""
        }
    """)

    if (functions.any { !it.isSuspend() }) return

    write("""

        public fun ${ServiceId::class.qualifiedName}<${service.name()}>.proxy(tunnel: $CSY.remote.Tunnel): ${service.name()} =
            object : ${service.name()} {
    """)
    functions.forEachIndexed { functionIndex, function ->
        val hasResult = function.hasResult()
        if (functionIndex != 0) appendLine()
        function.signature("        ")
        if (hasResult) append(" = ") else append(" {\n            ")
        append("tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(")
        function.parameters()
        append("))).process()")
        if (hasResult) append(" as ${function.returnType!!.name()}") else append("\n        }")
        appendLine()
    }
    write("""
        }
    """, 1)

    write("""

        public fun ${ServiceId::class.qualifiedName}<${service.name()}>.service(implementation: ${service.name()}): ${Service::class.qualifiedName} =
            ${Service::class.qualifiedName}(id) { functionId, parameters ->
                when (functionId) {
    """)
    functions.forEachIndexed { functionIndex, function ->
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

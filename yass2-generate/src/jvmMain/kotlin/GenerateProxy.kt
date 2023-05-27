package ch.softappeal.yass2.generate

import ch.softappeal.yass2.remote.*
import com.google.devtools.ksp.symbol.*

private val AnyFunctions = setOf("toString", "equals", "hashCode")

private fun KSFunctionDeclaration.isSuspend() = Modifier.SUSPEND in modifiers

internal fun Appendable.generateProxy(service: KSClassDeclaration) {
    require(service.classKind == ClassKind.INTERFACE) { "'${service.name()}' must be an interface" }

    val functions = service.getAllFunctions()
        .filter { it.toString() !in AnyFunctions }
        .toList()
        .sortedBy { it.toString() } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
        .apply {
            require(map { it.toString() }.toSet().size == size) { "'${service.name()}' has overloaded functions" }
        }

    fun KSFunctionDeclaration.hasResult() = "kotlin.Unit" != returnType!!.resolve().declaration.name()

    fun KSFunctionDeclaration.appendSignature(indent: String) {
        append("${indent}override ${if (isSuspend()) "suspend " else ""}fun $this(")
        parameters.forEachIndexed { parameterIndex, parameter ->
            if (parameterIndex != 0) append(", ")
            append("p${parameterIndex + 1}: ")
            appendType(parameter.type)
        }
        append(')')
    }

    fun KSFunctionDeclaration.appendParameters() {
        for (parameterIndex in 1..parameters.size) {
            if (parameterIndex != 1) append(", ")
            append("p$parameterIndex")
        }
    }

    run {
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
            function.appendSignature("    ")
            if (hasResult) {
                append(": ")
                appendType(function.returnType!!)
            }
            appendLine(" {")
            append("        ${if (hasResult) "return " else ""}${if (function.isSuspend()) "suspendIntercept" else "intercept"}(${service.name()}::$function, ")
            append("listOf(")
            function.appendParameters()
            append(")) { this@proxy.$function(")
            function.appendParameters()
            append(") }")
            if (hasResult) {
                append(" as ")
                appendType(function.returnType!!)
            }
            appendLine()
            appendLine("    }")
        }
        write("""
            }
        """)
    }

    if (functions.any { !it.isSuspend() }) return

    run {
        write("""

            public fun ${ServiceId::class.qualifiedName}<${service.name()}>.proxy(tunnel: $CSY.remote.Tunnel): ${service.name()} =
                object : ${service.name()} {
        """)
        functions.forEachIndexed { functionIndex, function ->
            val hasResult = function.hasResult()
            if (functionIndex != 0) appendLine()
            function.appendSignature("        ")
            if (hasResult) append(" = ") else append(" {\n            ")
            append("tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(")
            function.appendParameters()
            append("))).process()")
            if (hasResult) {
                append(" as ")
                appendType(function.returnType!!)
            } else append("\n        }")
            appendLine()
        }
        write("""
            }
        """, 1)
    }

    run {
        write("""

            public fun ${ServiceId::class.qualifiedName}<${service.name()}>.service(implementation: ${service.name()}): ${Service::class.qualifiedName} =
                ${Service::class.qualifiedName}(id) { functionId, parameters ->
                    when (functionId) {
        """)
        functions.forEachIndexed { functionIndex, function ->
            append("            $functionIndex -> implementation.$function(")
            function.parameters.forEachIndexed { parameterIndex, parameter ->
                if (parameterIndex != 0) append(", ")
                append("parameters[$parameterIndex] as ")
                appendType(parameter.type)
            }
            appendLine(')')
        }
        write("""
                    else -> error("service with id ${'$'}id has no function with id ${'$'}functionId")
                }
            }
        """, 1)
    }
}

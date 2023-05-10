package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.remote.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

private fun KClass<*>.serviceFunctions() = memberFunctions
    .filter { it.javaMethod!!.declaringClass != Object::class.java }
    .sortedBy(KFunction<*>::name) // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
    .apply {
        require(map(KFunction<*>::name).toSet().size == size) { "'${this@serviceFunctions}' has overloaded functions" }
    }

public fun Appendable.generateProxy(services: List<KClass<*>>) {
    require(services.toSet().size == services.size) { "duplicated service" }
    services.forEach { service ->
        generateLocalProxy(service)
        if (service.serviceFunctions().all(KFunction<*>::isSuspend)) {
            generateRemoteProxy(service)
            generateService(service)
        }
    }
}

private fun KFunction<*>.hasResult() = returnType.classifier != Unit::class

private fun Appendable.writeFunctionSignature(indent: String, function: KFunction<*>): Unit = with(function) {
    append("${indent}override ${if (function.isSuspend) "suspend " else ""}fun $name(")
    valueParameters.forEach { parameter ->
        if (parameter.index != 1) append(", ")
        append("p${parameter.index}: ${parameter.type}")
    }
    append(")")
}

private fun Appendable.parameterList(function: KFunction<*>) = function.valueParameters.forEach { parameter ->
    if (parameter.index != 1) append(", ")
    append("p${parameter.index}")
}

private fun Appendable.generateLocalProxy(service: KClass<*>) {
    write("""

        @Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "KotlinRedundantDiagnosticSuppress")
        public fun ${service.qualifiedName}.proxy(
    """)
    val functions = service.serviceFunctions()
    if (functions.any { !it.isSuspend }) appendLine("    intercept: $CSY.Interceptor,")
    if (functions.any { it.isSuspend }) appendLine("    suspendIntercept: $CSY.SuspendInterceptor,")
    write("""
        ): ${service.qualifiedName} = object : ${service.qualifiedName} {
    """)
    functions.forEachIndexed { functionIndex, function ->
        with(function) {
            if (functionIndex != 0) appendLine()
            writeFunctionSignature("    ", this)
            if (hasResult()) append(": $returnType")
            appendLine(" {")
            append("        ${if (hasResult()) "return " else ""}${if (isSuspend) "suspendIntercept" else "intercept"}(${service.qualifiedName}::$name, ")
            append("listOf(")
            parameterList(this)
            append(")) { this@proxy.$name(")
            parameterList(this)
            append(") }")
            if (hasResult() && returnType.needsCast()) append(" as $returnType")
            appendLine()
            appendLine("    }")
        }
    }
    write("""
        }
    """)
}

private fun Appendable.generateRemoteProxy(service: KClass<*>) {
    write("""

        @Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName}>.proxy(tunnel: $CSY.remote.Tunnel): ${service.qualifiedName} =
            object : ${service.qualifiedName} {
    """)
    service.serviceFunctions().forEachIndexed { functionIndex, function ->
        with(function) {
            if (functionIndex != 0) appendLine()
            writeFunctionSignature("        ", this)
            if (hasResult()) append(" = ") else append(" {\n            ")
            append("tunnel(${Request::class.qualifiedName}(id, $functionIndex, listOf(")
            parameterList(this)
            append("))).process()")
            if (hasResult() && returnType.needsCast()) append(" as $returnType")
            if (!hasResult()) append("\n        }")
            appendLine()
        }
    }
    write("""
        }
    """, 1)
}

private fun Appendable.generateService(service: KClass<*>) {
    write("""

        @Suppress("RedundantSuppression", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "RedundantNullableReturnType")
        public fun ${ServiceId::class.qualifiedName}<${service.qualifiedName}>.service(implementation: ${service.qualifiedName}): ${Service::class.qualifiedName} =
            ${Service::class.qualifiedName}(id) { functionId, parameters ->
                when (functionId) {
    """)
    service.serviceFunctions().forEachIndexed { functionIndex, function ->
        with(function) {
            append("            $functionIndex -> implementation.$name(")
            valueParameters.forEach { parameter ->
                if (parameter.index != 1) append(", ")
                append("parameters[${parameter.index - 1}]")
                if (parameter.type.needsCast()) append(" as ${parameter.type}")
            }
            appendLine(")")
        }
    }
    write("""
                else -> error("service with id ${'$'}id has no function with id ${'$'}functionId")
            }
        }
    """, 1)
}

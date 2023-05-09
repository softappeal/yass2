package ch.softappeal.yass2.ksp

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

internal fun KClass<*>.serviceFunctions(): List<KFunction<*>> = memberFunctions
    .filter { it.javaMethod!!.declaringClass != Object::class.java }
    .sortedBy { it.name } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
    .apply {
        require(map { it.name }.toSet().size == size) { "'${this@serviceFunctions}' has overloaded functions" }
    }

public fun Appendable.generateProxy(
    services: List<KClass<*>>,
) {
    require(services.toSet().size == services.size) { "duplicated service" }
    services.forEach { service ->
        write("""

            @Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
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
                val intercept = if (isSuspend) "suspendIntercept" else "intercept"
                val hasResult = if (hasResult()) "return " else ""
                appendLine(" {")
                append("        $hasResult$intercept(${service.qualifiedName}::${this.name}, ")
                fun KFunction<*>.parameterList() = valueParameters.forEach { parameter ->
                    if (parameter.index != 1) append(", ")
                    append("p${parameter.index}")
                }
                append("listOf(")
                parameterList()
                append(")) { this@proxy.${this.name}(")
                parameterList()
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
}

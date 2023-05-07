package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

internal fun KClass<*>.serviceFunctions(): List<KFunction<*>> = memberFunctions
    .filter { it.javaMethod!!.declaringClass != Object::class.java }
    .sortedBy { it.name } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
    .apply {
        require(map { it.name }.toSet().size == size) { "'${this@serviceFunctions}' has overloaded functions" }
    }

public fun Appendable.generateProxyFactory(
    services: List<KClass<*>>,
) {
    require(services.toSet().size == services.size) { "duplicated service" }
    write("""
        @Suppress("RedundantSuppression", "UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public object GeneratedProxyFactory : ${ProxyFactory::class.qualifiedName} {
            override fun <S : Any> create(
                service: ${KClass::class.qualifiedName}<S>, implementation: S,
                interceptor: $CSY.Interceptor, suspendInterceptor: $CSY.SuspendInterceptor,
            ): S = when (service) {
    """)
    services.forEach { service ->
        val functions = service.serviceFunctions()
        val needsInterceptor = "needsInterceptor = ${functions.any { !it.isSuspend }}"
        val needsSuspendInterceptor = "needsSuspendInterceptor = ${functions.any { it.isSuspend }}"
        write("""
            ${service.qualifiedName}::class -> object : ${service.qualifiedName} {
                init {
                    $CSY.checkInterceptors(interceptor, suspendInterceptor, $needsInterceptor, $needsSuspendInterceptor)
                }
        """, 2)
        functions.forEach {
            with(it) {
                appendLine()
                writeFunctionSignature("            ", this)
                if (hasResult()) append(": $returnType")
                val interceptor = if (isSuspend) "suspendInterceptor" else "interceptor"
                val hasResult = if (hasResult()) "return " else ""
                append(" {\n                $hasResult$interceptor(${service.qualifiedName}::${this.name}, ")
                fun KFunction<*>.parameterList() = valueParameters.forEach { parameter ->
                    if (parameter.index != 1) append(", ")
                    append("p${parameter.index}")
                }
                append("listOf(")
                parameterList()
                append(")) { (implementation as ${service.qualifiedName}).${this.name}(")
                parameterList()
                append(") }")
                if (hasResult() && returnType.needsCast()) append(" as $returnType")
                append("\n            }\n")
            }
        }
        write("""
            } as S
        """, 2)
    }
    write("""
                else -> error("no proxy for '${'$'}service'")
            }
        }
    """)
}

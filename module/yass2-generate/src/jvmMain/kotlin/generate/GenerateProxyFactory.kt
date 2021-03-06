package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import ch.softappeal.yass2.reflect.*
import kotlin.reflect.*
import kotlin.reflect.full.*

public fun generateProxyFactory(
    services: List<KClass<*>>,
    name: String = "GeneratedProxyFactory",
): String = writer {
    require(services.toSet().size == services.size) { "duplicated services" }
    write("""
        @Suppress("UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public object $name : ${ProxyFactory::class.qualifiedName} {
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

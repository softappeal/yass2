package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

internal fun StringBuilder.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal fun writer(action: StringBuilder.() -> Unit): String = StringBuilder(10_000).apply { action() }.toString()

internal const val CSY = "ch.softappeal.yass2"

internal fun KClass<*>.serviceFunctions(): List<KFunction<*>> = memberFunctions
    .filter { it.javaMethod!!.declaringClass != Object::class.java }
    .sortedBy { it.name } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
    .apply {
        require(map { it.name }.toSet().size == size) { "'${this@serviceFunctions}' has overloaded functions" }
    }
    .onEach {
        require(it.isSuspend) { "'$it' is not a suspend function" }
    }

internal fun StringBuilder.writeFunctionSignature(indent: String, function: KFunction<*>): Unit = with(function) {
    append("${indent}override ${if (function.isSuspend) "suspend " else ""}fun $name(")
    valueParameters.forEach { parameter ->
        if (parameter.index != 1) append(", ")
        append("p${parameter.index}: ${parameter.type}")
    }
    append(")")
}

internal fun KFunction<*>.hasResult(): Boolean = returnType.classifier != Unit::class

public fun generateProxyFactory(
    services: List<KClass<*>>,
    name: String = "ProxyFactory",
): String = writer {
    require(services.toSet().size == services.size) { "duplicated service" }
    write("""
        @Suppress("UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public object $name : ${ProxyFactory::class.qualifiedName} {
            override fun <S : Any> create(
                service: ${KClass::class.qualifiedName}<S>, implementation: S, interceptor: $CSY.Interceptor,
            ): S = when (service) {
    """)
    services.forEach { service ->
        write("""
            ${service.qualifiedName}::class -> object : ${service.qualifiedName} {
        """, 2)
        service.serviceFunctions().withIndex().forEach { (functionIndex, function) ->
            with(function) {
                if (functionIndex != 0) appendLine()
                writeFunctionSignature("            ", this)
                if (hasResult()) append(": $returnType")
                val hasResult = if (hasResult()) "return " else ""
                append(" {\n                ${hasResult}interceptor(${service.qualifiedName}::${this.name}, ")
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

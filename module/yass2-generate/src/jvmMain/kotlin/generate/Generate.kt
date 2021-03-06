package ch.softappeal.yass2.generate

import kotlin.reflect.*
import kotlin.reflect.full.*

internal fun KFunction<*>.hasResult(): Boolean = returnType.classifier != Unit::class

internal fun KType.needsCast(): Boolean = classifier != Any::class || !isMarkedNullable

internal fun StringBuilder.writeFunctionSignature(indent: String, function: KFunction<*>): Unit = with(function) {
    append("${indent}override ${if (function.isSuspend) "suspend " else ""}fun $name(")
    valueParameters.forEach { parameter ->
        if (parameter.index != 1) append(", ")
        append("p${parameter.index}: ${parameter.type}")
    }
    append(")")
}

internal fun StringBuilder.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal fun writer(action: StringBuilder.() -> Unit): String = StringBuilder(10_000).apply { action() }.toString()

internal const val CSY = "ch.softappeal.yass2"

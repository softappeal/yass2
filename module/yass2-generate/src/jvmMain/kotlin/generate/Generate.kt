package ch.softappeal.yass2.generate

import java.io.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

internal fun StringBuilder.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal fun writer(action: StringBuilder.() -> Unit): String = StringBuilder(10_000).apply { action() }.toString()

internal const val CSY = "ch.softappeal.yass2"

internal fun KType.needsCast(): Boolean = classifier != Any::class || !isMarkedNullable

internal fun KFunction<*>.hasResult(): Boolean = returnType.classifier != Unit::class

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

public enum class GenerateAction {
    Write {
        override fun execute(filePath: String, code: String) {
            File(filePath).writeText(code)
        }
    },
    Verify {
        override fun execute(filePath: String, code: String) {
            val existingCode = File(filePath).readText().replace("\r\n", "\n")
            check(code == existingCode) {
                "file '$filePath' is\n${">".repeat(120)}\n$existingCode\n${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$code\n${"<".repeat(120)}"
            }
        }
    };

    public abstract fun execute(filePath: String, code: String)
}

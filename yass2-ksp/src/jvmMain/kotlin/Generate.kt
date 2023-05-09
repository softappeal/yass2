package ch.softappeal.yass2.ksp

import java.nio.file.*
import kotlin.io.path.*
import kotlin.reflect.*
import kotlin.reflect.full.*

internal fun Appendable.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal const val CSY = "ch.softappeal.yass2"

@Suppress("BooleanMethodIsAlwaysInverted")
internal fun KType.needsCast(): Boolean = classifier != Any::class || !isMarkedNullable

@Suppress("BooleanMethodIsAlwaysInverted")
internal fun KFunction<*>.hasResult(): Boolean = returnType.classifier != Unit::class

internal fun Appendable.writeFunctionSignature(indent: String, function: KFunction<*>): Unit = with(function) {
    append("${indent}override ${if (function.isSuspend) "suspend " else ""}fun $name(")
    valueParameters.forEach { parameter ->
        if (parameter.index != 1) append(", ")
        append("p${parameter.index}: ${parameter.type}")
    }
    append(")")
}

public fun generate(sourceDir: Path, packageName: String, fileName: String, code: Appendable.() -> Unit) {
    val builder = StringBuilder()
    builder.appendLine("package $packageName")
    builder.code()
    Files.createDirectories(sourceDir)
    sourceDir.resolve("$fileName.kt").writeText(builder.toString())
}

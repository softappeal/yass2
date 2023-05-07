package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.binary.*
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
    builder.appendLine()
    builder.code()
    Files.createDirectories(sourceDir)
    sourceDir.resolve("$fileName.kt").writeText(builder.toString())
}

public fun generateAll(
    sourceDir: Path, packageName: String,
    serviceIds: List<ServiceId<out Any>>,
    baseEncodersProperty: KProperty0<List<BaseEncoder<out Any>>>, treeConcreteClasses: List<KClass<out Any>>, graphConcreteClasses: List<KClass<out Any>> = emptyList(),
) {
    generate(sourceDir, packageName, "GeneratedProxyFactory") { generateProxyFactory(serviceIds.map { it.service }) }
    generate(sourceDir, packageName, "GeneratedRemote") { generateRemote(serviceIds) }
    generate(sourceDir, packageName, "GeneratedBinarySerializer") { generateBinarySerializer(baseEncodersProperty, treeConcreteClasses, graphConcreteClasses) }
    generate(sourceDir, packageName, "GeneratedDumperProperties") { generateDumperProperties(treeConcreteClasses + graphConcreteClasses) }
}

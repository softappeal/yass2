package ch.softappeal.yass2.generate

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.binary.*
import java.nio.file.*
import kotlin.io.path.*
import kotlin.reflect.*
import kotlin.reflect.full.*

internal fun String.firstCharToUppercase() = this.replaceFirstChar(Char::uppercaseChar)
internal fun String.firstCharToLowercase() = this.replaceFirstChar(Char::lowercaseChar)

public const val GENERATED: String = "generated"

internal fun StringBuilder.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal fun writer(action: StringBuilder.() -> Unit): String = StringBuilder(10_000).apply { action() }.toString()

internal const val CSY = "ch.softappeal.yass2"

@Suppress("BooleanMethodIsAlwaysInverted")
internal fun KType.needsCast(): Boolean = classifier != Any::class || !isMarkedNullable

@Suppress("BooleanMethodIsAlwaysInverted")
internal fun KFunction<*>.hasResult(): Boolean = returnType.classifier != Unit::class

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
        override fun execute(file: Path, code: String) {
            Files.createDirectories(file.parent)
            file.writeText(code)
        }
    },
    Verify {
        override fun execute(file: Path, code: String) {
            val existingCode = file.readText().replace("\r\n", "\n")
            check(code == existingCode) {
                "file '$file' is\n${">".repeat(120)}\n$existingCode\n${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$code\n${"<".repeat(120)}"
            }
        }
    },
    ;

    public abstract fun execute(file: Path, code: String)
}

public fun GenerateAction.all(
    dir: Path, pkg: String,
    serviceIds: List<ServiceId<out Any>>,
    baseEncodersProperty: KProperty0<List<BaseEncoder<out Any>>>, treeConcreteClasses: List<KClass<out Any>>, graphConcreteClasses: List<KClass<out Any>> = emptyList(),
    generated: String = GENERATED,
) {
    fun execute(fileName: String, code: String) = execute(dir.resolve("${generated.firstCharToUppercase()}$fileName.kt"), "package $pkg\n\n$code")
    execute(PROXY_FACTORY, generateProxyFactory(serviceIds.map { it.service }, generated + PROXY_FACTORY))
    execute("Remote", generateRemoteProxyFactory(serviceIds, generated + REMOTE_PROXY_FACTORY) + "\n" + generateInvoke(serviceIds, generated + INVOKE))
    execute(BINARY_SERIALIZER, generateBinarySerializer(baseEncodersProperty, treeConcreteClasses, graphConcreteClasses, generated + BINARY_SERIALIZER))
    execute(DUMPER_PROPERTIES, generateDumperProperties(treeConcreteClasses + graphConcreteClasses, generated + DUMPER_PROPERTIES))
}

package ch.softappeal.yass2.generate

import ch.softappeal.yass2.remote.*
import ch.softappeal.yass2.serialize.binary.*
import java.io.*
import java.nio.file.*
import kotlin.reflect.*
import kotlin.reflect.full.*

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
        override fun execute(filePath: String, code: String) {
            val file = File(filePath)
            Files.createDirectories(file.parentFile.toPath())
            file.writeText(code)
        }
    },
    Verify {
        override fun execute(filePath: String, code: String) {
            val existingCode = File(filePath).readText().replace("\r\n", "\n")
            check(code == existingCode) {
                "file '$filePath' is\n${">".repeat(120)}\n$existingCode\n${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$code\n${"<".repeat(120)}"
            }
        }
    },
    ;

    public abstract fun execute(filePath: String, code: String)
}

public fun GenerateAction.all(
    dir: String, pkg: String,
    serviceIds: List<ServiceId<out Any>>,
    baseEncoders: List<BaseEncoder<out Any>>, treeConcreteClasses: List<KClass<out Any>>, graphConcreteClasses: List<KClass<out Any>> = emptyList(),
) {
    fun execute(fileName: String, code: String): Unit = this.execute("$dir/$fileName", "package $pkg\n\n$code")
    execute("GeneratedProxyFactory.kt", generateProxyFactory(serviceIds.map { it.service }))
    execute("GeneratedRemote.kt", generateRemoteProxyFactory(serviceIds) + "\n" + generateInvoke(serviceIds))
    execute("GeneratedBinarySerializer.kt", generateBinarySerializer(baseEncoders, treeConcreteClasses, graphConcreteClasses))
    execute("GeneratedDumperProperties.kt", generateDumperProperties(treeConcreteClasses + graphConcreteClasses))
}

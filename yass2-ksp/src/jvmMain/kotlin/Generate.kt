package ch.softappeal.yass2.ksp

import java.nio.file.*
import kotlin.io.path.*
import kotlin.reflect.*

internal fun Appendable.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal const val CSY = "ch.softappeal.yass2"

internal fun KType.needsCast() = classifier != Any::class || !isMarkedNullable

private const val VERIFY = true

public fun generate(sourceDir: Path, packageName: String, fileName: String, code: Appendable.() -> Unit) {
    val builder = StringBuilder()
    builder.appendLine("package $packageName")
    builder.code()
    val program = builder.toString()
    val file = sourceDir.resolve("$fileName.kt")
    if (VERIFY) {
        val existingProgram = file.readText().replace("\r\n", "\n")
        check(program == existingProgram) {
            "file '$file' is\n${">".repeat(120)}\n$existingProgram${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$program${"<".repeat(120)}"
        }
    } else {
        Files.createDirectories(sourceDir)
        file.writeText(program)
    }
}

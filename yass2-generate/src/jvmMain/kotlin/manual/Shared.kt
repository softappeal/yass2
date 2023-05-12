package ch.softappeal.yass2.generate.manual

import java.nio.file.*
import kotlin.io.path.*
import kotlin.reflect.*

internal fun KType.needsCast() = classifier != Any::class || !isMarkedNullable

public enum class Mode { Verify, Write }

private const val FORCE_WRITE = false

public fun generate(mode: Mode, sourceDir: Path, packageName: String, fileName: String, code: Appendable.() -> Unit) {
    val builder = StringBuilder()
    builder.appendLine("package $packageName")
    builder.code()
    val program = builder.toString()
    val file = sourceDir.resolve("$fileName.kt")
    when (if (FORCE_WRITE) Mode.Write else mode) {
        Mode.Verify -> {
            val existingProgram = file.readText().replace("\r\n", "\n")
            check(program == existingProgram) {
                "file '$file' is\n${">".repeat(120)}\n$existingProgram${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$program${"<".repeat(120)}"
            }
        }
        Mode.Write -> {
            Files.createDirectories(sourceDir)
            file.writeText(program)
        }
    }
}

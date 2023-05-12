package ch.softappeal.yass2.generate.manual

import ch.softappeal.yass2.generate.*
import java.nio.file.*
import kotlin.io.path.*

public enum class Mode { Verify, Write }

private const val FORCE_WRITE = false

public fun generate(mode: Mode, sourceDir: Path, packageName: String, fileName: String, code: Appendable.() -> Unit) {
    val builder = StringBuilder()
    builder.writeHeader(packageName)
    builder.code()
    val program = builder.toString()
    val file = sourceDir.resolve("$fileName.kt")
    when (if (FORCE_WRITE) Mode.Write else mode) {
        Mode.Verify -> file.verify(program)
        Mode.Write -> {
            Files.createDirectories(sourceDir)
            file.writeText(program)
        }
    }
}

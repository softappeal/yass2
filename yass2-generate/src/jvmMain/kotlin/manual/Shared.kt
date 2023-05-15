package ch.softappeal.yass2.generate.manual

import ch.softappeal.yass2.generate.*
import java.nio.file.*
import kotlin.io.path.*

private fun Path.readAndFixLines(): String = readText().replace("\r\n", "\n")

private fun Path.verify(code: String) {
    val existingCode = readAndFixLines()
    check(code == existingCode) {
        "file '$this' is\n${">".repeat(120)}\n$existingCode${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$code${"<".repeat(120)}"
    }
}

public fun verify(sourceDir: String, fileName: String, packageName: String, code: Appendable.() -> Unit) {
    val builder = StringBuilder()
    builder.writeHeader(packageName)
    builder.code()
    Path(sourceDir).resolve("$fileName.kt").verify(builder.toString())
}

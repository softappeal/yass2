package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal fun <T> List<T>.hasNoDuplicates() = size == toSet().size

internal fun <T> List<T>.duplicates(): List<T> {
    val seen = HashSet<T>()
    return filter { !seen.add(it) }
}

public fun KType.toType(): String = toString() // TODO: see file 'KTypeToTypeTest.kt'
    .replace("kotlin.Exception /* = java.lang.Exception */", "kotlin.Exception")

public fun CodeWriter.generateProxies(services: List<KClass<*>>) {
    services.forEach(::generateProxy)
}

public fun generateFile(generatedDir: String, packageName: String, write: CodeWriter.() -> Unit) {
    val builder = StringBuilder()
    builder.appendPackage(packageName)
    CodeWriter(builder).write()
    val generatedFile = Path(generatedDir).resolve(GENERATED_BY_YASS)
    Files.createDirectories(generatedFile.parent)
    generatedFile.writeText(builder.toString())
}

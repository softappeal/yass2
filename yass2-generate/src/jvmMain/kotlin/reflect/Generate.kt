package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.appendPackage
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal fun KType.toType() = toString() // TODO: see file 'KTypeToTypeTest.kt'
    .replace("kotlin.Exception /* = java.lang.Exception */", "kotlin.Exception")

public fun generateFile(generatedDir: String, packageName: String, write: CodeWriter.() -> Unit) {
    val builder = StringBuilder()
    builder.appendPackage(packageName)
    @OptIn(InternalApi::class) CodeWriter(builder).write()
    val generatedFile = Path(generatedDir).resolve("$GENERATED_BY_YASS.kt")
    Files.createDirectories(generatedFile.parent)
    generatedFile.writeText(builder.toString())
}

public fun CodeWriter.generateProxies(services: List<KClass<*>>) {
    services.forEach(::generateProxy)
}

@OptIn(InternalApi::class)
public fun CodeWriter.generateBinarySerializer(annotatedClass: KClass<*>) {
    generateBinarySerializer(
        annotatedClass.java.getAnnotation(BinaryEncoderObjects::class.java)!!.value.toList(),
        annotatedClass.java.getAnnotation(ConcreteAndEnumClasses::class.java)!!.value.toList(),
    )
}

@OptIn(InternalApi::class)
public fun CodeWriter.generateStringEncoders(annotatedClass: KClass<*>) {
    generateStringEncoders(
        annotatedClass.java.getAnnotation(StringEncoderObjects::class.java)!!.value.toList(),
        annotatedClass.java.getAnnotation(ConcreteAndEnumClasses::class.java)!!.value.toList(),
    )
}

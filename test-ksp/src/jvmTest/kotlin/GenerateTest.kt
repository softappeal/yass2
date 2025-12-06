package test

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateCode
import ch.softappeal.yass2.generate.reflect.generateProxies
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateTest {
    @Test
    fun generate() {
        val code = generateCode {
            generateProxies(Generate::class)
            generateBinarySerializer(Generate::class)
            generateStringEncoders(Generate::class)
        }
        File("build/generated/ksp").listFiles()?.forEach { file ->
            val platform = file.name
            if (platform == "metadata") return@forEach
            println("platform $platform")
            assertEquals(
                code,
                File("build/generated/ksp/$platform/${platform}Main/kotlin/test/$GENERATED_BY_YASS.kt").readText(),
            )
        }
    }
}

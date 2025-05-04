package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.core.Proxy
import ch.softappeal.yass2.core.serialize.ConcreteAndEnumClasses
import ch.softappeal.yass2.core.serialize.binary.BinaryEncoderObjects
import ch.softappeal.yass2.core.serialize.string.StringEncoderObjects
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.reflect.generateBinarySerializer
import ch.softappeal.yass2.generate.reflect.generateFile
import ch.softappeal.yass2.generate.reflect.generateProxy
import ch.softappeal.yass2.generate.reflect.generateStringEncoders
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

@Proxy
interface Calculator {
    suspend fun add(a: Int, b: Int): Int
}

class A

@ConcreteAndEnumClasses(A::class)
@BinaryEncoderObjects()
@StringEncoderObjects()
private class Generate

class PlatformTest {
    @Test
    fun test() {
        generateFile(
            "src/jvmTest/kotlin/generate/ksp/reflect",
            "ch.softappeal.yass2.generate.ksp.reflect",
        ) {
            generateProxy(Calculator::class)
            generateBinarySerializer(Generate::class)
            generateStringEncoders(Generate::class)
        }

        assertEquals(
            Path("src/jvmTest/kotlin/generate/ksp/reflect/$GENERATED_BY_YASS.kt").readText(),
            Path("build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/generate/ksp/$GENERATED_BY_YASS.kt").readText()
                .replace(
                    "package ch.softappeal.yass2.generate.ksp",
                    "package ch.softappeal.yass2.generate.ksp.reflect",
                ),
        )
    }
}

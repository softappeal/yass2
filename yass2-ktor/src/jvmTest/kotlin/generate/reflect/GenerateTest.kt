package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.ContractSerializer
import ch.softappeal.yass2.Services
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateTest {
    @Test
    fun test() {
        generateFile(
            "src/jvmTest/kotlin/generate/reflect",
            "ch.softappeal.yass2.generate.reflect",
        ) {
            generateProxies(Services)
            generateBinarySerializer(::ContractSerializer)
            generateStringEncoders(::ContractSerializer)
        }

        assertEquals(
            Path("src/jvmTest/kotlin/generate/reflect/$GENERATED_BY_YASS.kt").readText()
                .replace(
                    "package ch.softappeal.yass2.generate.reflect",
                    "package ch.softappeal.yass2",
                ),
            Path("src/commonTest/kotlin/$GENERATED_BY_YASS.kt").readText(),
        )
    }
}

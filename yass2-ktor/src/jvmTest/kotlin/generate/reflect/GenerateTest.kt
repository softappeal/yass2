package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Generate
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
            generateBinarySerializer(Generate::class)
            generateStringEncoders(Generate::class)
        }

        assertEquals(
            Path("src/jvmTest/kotlin/generate/reflect/$GENERATED_BY_YASS.kt").readText() +
                """

                    /* copy to common code

                    public expect fun ch.softappeal.yass2.Calculator.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.Calculator
                    public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.Calculator
                    public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.service(implementation: ch.softappeal.yass2.Calculator): ch.softappeal.yass2.core.remote.Service

                    public expect fun ch.softappeal.yass2.Echo.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.Echo
                    public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.Echo
                    public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.service(implementation: ch.softappeal.yass2.Echo): ch.softappeal.yass2.core.remote.Service

                    public expect fun createBinarySerializer(): ch.softappeal.yass2.core.serialize.binary.BinarySerializer

                    public expect fun createStringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>>

                    */

                """.trimIndent(),
            Path("build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/$GENERATED_BY_YASS.kt").readText()
                .replace(
                    "package ch.softappeal.yass2",
                    "package ch.softappeal.yass2.generate.reflect",
                )
                .replace(
                    "public actual fun ",
                    "public fun ",
                )
        )
    }
}

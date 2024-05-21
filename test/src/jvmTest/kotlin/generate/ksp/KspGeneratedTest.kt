package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.GENERATED_BINARY_SERIALIZER
import ch.softappeal.yass2.generate.GENERATED_DUMPER
import ch.softappeal.yass2.generate.GENERATED_PROXY
import ch.softappeal.yass2.generate.reflect.readAndFixLines
import kotlin.io.path.Path
import kotlin.test.Test

class KspGeneratedTest {
    @Test
    fun test() {
        fun verify(fileName: String, child: Boolean = false) {
            val newGenerated =
                Path("build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/contract${if (child) "/child" else ""}/$fileName.kt")
                    .readAndFixLines()
            val oldGenerated =
                Path("src/commonTest/kotlin/contract${if (child) "/child" else ""}/generated/$fileName.kt")
                    .readAndFixLines()
                    .replace(
                        "package ch.softappeal.yass2.contract${if (child) ".child" else ""}.generated",
                        "package ch.softappeal.yass2.contract${if (child) ".child" else ""}",
                    )
            check(newGenerated == oldGenerated) {
                "'$fileName' is\n${">".repeat(120)}\n$newGenerated${"<".repeat(120)}\nbut should be\n${
                    ">".repeat(120)
                }\n$oldGenerated${"<".repeat(120)}"
            }
        }
        verify(GENERATED_BINARY_SERIALIZER)
        verify(GENERATED_PROXY)
        verify(GENERATED_DUMPER)
        verify(GENERATED_PROXY, true)
        verify(GENERATED_DUMPER, true)
    }
}

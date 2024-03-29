package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.readAndFixLines
import kotlin.io.path.Path
import kotlin.test.Test

class VerifyGeneratedTest {
    @Test
    fun test() {
        fun verify(fileName: String) {
            val newGenerated =
                Path("build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/contract/$fileName.kt").readAndFixLines()
            val oldGenerated = Path("src/commonTest/kotlin/contract/generated/$fileName.kt").readAndFixLines()
                .replace("package ch.softappeal.yass2.contract.generated", "package ch.softappeal.yass2.contract")
            check(newGenerated == oldGenerated) {
                "$fileName is\n${">".repeat(120)}\n$newGenerated${"<".repeat(120)}\nbut should be\n" +
                    "${">".repeat(120)}\n$oldGenerated${"<".repeat(120)}"
            }
        }
        verify(GENERATED_PROXY)
        verify(GENERATED_BINARY_SERIALIZER)
        verify(GENERATED_DUMPER)
    }
}

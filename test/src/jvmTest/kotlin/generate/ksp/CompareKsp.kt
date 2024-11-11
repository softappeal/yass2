package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.readAndFixLines
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

private fun verify(kspPath: String, reflectPath: String, packageName: String) {
    val ksp = Path("$kspPath/$GENERATED_BY_YASS.kt")
        .readAndFixLines()
    val reflect = Path("$reflectPath/reflect/$GENERATED_BY_YASS.kt")
        .readAndFixLines()
        .replace(
            "package ch.softappeal.yass2.$packageName.reflect",
            "package ch.softappeal.yass2.$packageName",
        )
    assertEquals(reflect, ksp)
}

class CompareKsp {
    @Test
    fun test() {
        listOf(false, true).forEach { child ->
            verify(
                "build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/contract${if (child) "/child" else ""}",
                "src/commonTest/kotlin/contract${if (child) "/child" else ""}",
                "contract${if (child) ".child" else ""}",
            )
        }
    }
}

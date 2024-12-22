package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import ch.softappeal.yass2.generate.readAndFixLines
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class CompareKsp {
    @Test
    fun test() {
        val ksp = Path("build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/contract/$GENERATED_BY_YASS.kt")
            .readAndFixLines()
        val reflect = Path("src/commonTest/kotlin/contract/reflect/$GENERATED_BY_YASS.kt")
            .readAndFixLines()
            .replace(
                "package ch.softappeal.yass2.contract.reflect",
                "package ch.softappeal.yass2.contract",
            )
            .replace(
                "public fun ",
                "public actual fun ",
            )
        assertEquals(reflect, ksp)
    }
}

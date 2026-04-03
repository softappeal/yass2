// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Generate
import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private const val DIR = "src/jvmTest/kotlin/generate/reflect"

val CODE = File("$DIR/$GENERATED_BY_YASS.kt")
    .readText()
    .replace("package ch.softappeal.yass2.generate.reflect\n", "package ch.softappeal.yass2\n")

class GenerateTest {
    @OptIn(InternalApi::class)
    @Test
    fun removeComment() {
        assertEquals("kotlin.Exception", "kotlin.Exception /* = java.lang.Exception */".removeComment())
        assertEquals("", "/**/".removeComment())
        assertEquals("", "  /*  */  ".removeComment())
        assertEquals("abc.123  xyz", "  abc.123  xyz  /*  d dkj 12 *  /  */  ".removeComment())
    }

    @Test
    fun generateFile() {
        generateFile(DIR, Generate::class)
    }
}

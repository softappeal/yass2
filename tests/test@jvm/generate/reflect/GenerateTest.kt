// TODO: run tests with coverage manually in IntelliJ

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.Generate
import ch.softappeal.yass2.core.TestingYassApi
import ch.softappeal.yass2.generate.GENERATED_BY_YASS
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private const val DIR = "test@jvm/generate/reflect"

val CODE = File("$DIR/$GENERATED_BY_YASS.kt")
    .readText()
    .replace("package ch.softappeal.yass2.generate.reflect\n", "package ch.softappeal.yass2\n")

private fun map(): Map<Exception, RuntimeException>? = null
private const val MapType =
    "kotlin.collections.Map<kotlin.Exception /* = java.lang.Exception */, kotlin.RuntimeException /* = java.lang.RuntimeException */>?"

class GenerateTest {
    @OptIn(TestingYassApi::class)
    @Test
    fun removeComment() {
        assertEquals("", "/**/".removeComment())
        assertEquals("", " /**/".removeComment())
        assertEquals("", "  /**/".removeComment())
        assertEquals("12", "1 /*abc/**/2".removeComment())
        assertEquals("kotlin.Exception", "kotlin.Exception /* = java.lang.Exception */".removeComment())
        assertEquals(MapType, ::map.returnType.toString())
        assertEquals("kotlin.collections.Map<kotlin.Exception, kotlin.RuntimeException>?", MapType.removeComment())
        assertEquals(" 12 ", " 1  /*abc/* a b c * / */2 ".removeComment())
    }

    @Test
    fun generateFile() {
        generateFile(DIR, Generate::class)
    }
}

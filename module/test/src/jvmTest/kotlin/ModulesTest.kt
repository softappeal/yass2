package ch.softappeal.yass2

import java.io.*
import kotlin.test.*

private fun printModules(directory: String): String {
    val b = StringBuffer()
    printModules(directory) { b.append(it) }
    return b.toString()
}

class ModulesTest {
    @Test
    fun test() {
        assertEquals(printModules(".."), File("src/jvmTest/resources/modules.txt").readText().replace("\r\n", "\n"))
    }

    @Test
    fun notMain() {
        assertEquals(
            "target 'testTest' must end with 'Main'",
            assertFailsWith<IllegalStateException> { printModules("src/jvmTest/resources/notMain") }.message
        )
    }

    @Test
    fun split1() {
        assertEquals(
            "modules 'module1' and 'module2' have split package 'kotlin/Test.kt'",
            assertFailsWith<IllegalStateException> { printModules("src/jvmTest/resources/split1") }.message
        )
    }

    @Test
    fun split2() {
        assertEquals(
            "modules 'module1' and 'module2' have split package 'kotlin/Test2.kt'",
            assertFailsWith<IllegalStateException> { printModules("src/jvmTest/resources/split2") }.message
        )
    }

    @Test
    fun targets() {
        assertEquals(
            """
                . module:module1
                    Test.kt targets:[common, jvm]

            """.trimIndent(),
            printModules("src/jvmTest/resources/targets")
        )
    }
}

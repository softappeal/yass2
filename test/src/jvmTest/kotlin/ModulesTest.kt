package ch.softappeal.yass2

import java.io.*
import kotlin.test.*

private val Modules = setOf("yass2-core", "yass2-coroutines", "yass2-generate", "yass2-ktor", "yass2-reflect")

private fun printModules(directory: String, modules: Set<String>? = null) = StringBuffer().apply {
    printModules(modules, directory) { append(it) }
}.toString()

class ModulesTest {
    @Test
    fun test() {
        assertEquals(File("src/jvmTest/resources/modules.md").readText().replace("\r\n", "\n"), printModules("..", Modules))
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
                - . `module1`
                    - Test.kt `[common, jvm]`

            """.trimIndent(),
            printModules("src/jvmTest/resources/targets")
        )
    }

    @Test
    fun noModule() {
        @Suppress("SpellCheckingInspection")
        assertEquals(
            """
                - . `<no-module>`
                    - nofile `module1`
                        - Test.kt `[common]`

            """.trimIndent(),
            printModules("src/jvmTest/resources/noModule")
        )
    }
}

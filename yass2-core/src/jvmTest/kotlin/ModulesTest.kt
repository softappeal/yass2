package ch.softappeal.yass2

import java.io.*
import kotlin.io.path.*
import kotlin.test.*

private val Modules = setOf("yass2-core", "yass2-coroutines", "yass2-ktor")

private fun assertModules(expected: String, directory: String, modules: Set<String>? = null) {
    val charset = Charsets.UTF_8
    val bytes = ByteArrayOutputStream()
    val out = System.out
    System.setOut(PrintStream(bytes, true, charset))
    try {
        Path(directory).printModules(modules)
    } finally {
        System.setOut(out)
    }
    assertEquals((expected + "\n").trimIndent(), bytes.toString(charset))
}

private inline fun <reified T : Throwable> assertFailsMessage(expectedMessage: String, block: () -> Unit) =
    assertEquals(expectedMessage, assertFailsWith(T::class, block).message)

class ModulesTest {
    @Test
    fun test() {
        assertModules(Path("src/commonTest/resources/modules.md").readText().replace("\r\n", "\n"), "..", Modules)
    }

    @Test
    fun notMain() = assertFailsMessage<IllegalStateException>("target 'testWrong' must end with 'Main'") {
        Path("src/commonTest/resources/notMain").printModules()
    }

    @Test
    fun split1() = assertFailsMessage<IllegalStateException>("modules 'module1' and 'module2' have split package 'kotlin/Test.kt'") {
        Path("src/commonTest/resources/split1").printModules()
    }

    @Test
    fun split2() = assertFailsMessage<IllegalStateException>("modules 'module1' and 'module2' have split package 'kotlin/Test2.kt'") {
        Path("src/commonTest/resources/split2").printModules()
    }

    @Test
    fun targets() {
        assertModules(
            """
                - . `module1`
                    - Test.kt `[common, jvm]`
            """,
            "src/commonTest/resources/targets"
        )
    }

    @Test
    fun noModule() {
        @Suppress("SpellCheckingInspection")
        assertModules(
            """
                - . `<no-module>`
                    - nofile `module1`
                        - Test.kt `[common]`
            """,
            "src/commonTest/resources/noModule"
        )
    }
}

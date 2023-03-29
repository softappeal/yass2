package ch.softappeal.yass2

import java.io.*
import kotlin.io.path.*
import kotlin.test.*

private val Modules = setOf("yass2-core", "yass2-coroutines", "yass2-generate", "yass2-ktor", "yass2-reflect")

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

class ModulesTest {
    @Test
    fun test() {
        assertModules(Path("src/jvmTest/resources/modules.md").readText().replace("\r\n", "\n"), "..", Modules)
    }

    @Test
    fun notMain() = assertFailsMessage<IllegalStateException>("target 'testTest' must end with 'Main'") {
        Path("src/jvmTest/resources/notMain").printModules()
    }

    @Test
    fun split1() = assertFailsMessage<IllegalStateException>("modules 'module1' and 'module2' have split package 'kotlin/Test.kt'") {
        Path("src/jvmTest/resources/split1").printModules()
    }

    @Test
    fun split2() = assertFailsMessage<IllegalStateException>("modules 'module1' and 'module2' have split package 'kotlin/Test2.kt'") {
        Path("src/jvmTest/resources/split2").printModules()
    }

    @Test
    fun targets() {
        assertModules(
            """
                - . `module1`
                    - Test.kt `[common, jvm]`
            """,
            "src/jvmTest/resources/targets"
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
            "src/jvmTest/resources/noModule"
        )
    }
}

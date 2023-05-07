package ch.softappeal.yass2.contract

import ch.softappeal.yass2.ksp.*
import kotlin.io.path.*
import kotlin.test.*

private fun generate(fileName: String, code: Appendable.() -> Unit) {
    generate(Path("src/commonTest/kotlin/contract"), "ch.softappeal.yass2.contract", fileName, code)
}

class GenerateTest {
    @Test
    fun generateProxyFactory() {
        generate("GeneratedProxyFactory") { generateProxyFactory(ServiceIds.map { it.service } + Mixed::class) }
    }

    @Test
    fun generateRemote() {
        generate("GeneratedRemote") { generateRemote(ServiceIds) }
    }

    @Test
    fun generateBinarySerializer() {
        generate("GeneratedBinarySerializer") { generateBinarySerializer(::BaseEncoders, TreeConcreteClasses, GraphConcreteClasses) }
    }

    @Test
    fun generateDumper() {
        generate("GeneratedDumperProperties") { generateDumperProperties(TreeConcreteClasses + GraphConcreteClasses) }
    }
}

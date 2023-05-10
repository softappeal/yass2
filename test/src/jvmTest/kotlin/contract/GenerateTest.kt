package ch.softappeal.yass2.contract

import ch.softappeal.yass2.ksp.*
import kotlin.io.path.*
import kotlin.test.*

class GenerateTest {
    @Test
    fun test() {
        fun generate(fileName: String, code: Appendable.() -> Unit) = generate(Path("src/commonTest/kotlin/contract"), "ch.softappeal.yass2.contract", fileName, code)
        generate("GeneratedProxy") { generateProxy(Services) }
        generate("GeneratedBinarySerializer") { generateBinarySerializer(::BaseEncoders, TreeConcreteClasses, GraphConcreteClasses) }
        generate("GeneratedDumperProperties") { generateDumperProperties(TreeConcreteClasses + GraphConcreteClasses) }
    }
}

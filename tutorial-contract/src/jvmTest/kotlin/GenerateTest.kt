package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.generate.manual.*
import kotlin.io.path.*
import kotlin.test.*

class GenerateTest {
    @Test
    fun test() {
        fun generate(fileName: String, code: Appendable.() -> Unit) = generate(Mode.Verify, Path("src/commonMain/kotlin"), "ch.softappeal.yass2.tutorial.contract", fileName, code)
        generate("GeneratedProxy") { generateProxy(Services) }
        generate("GeneratedBinarySerializer") { generateBinarySerializer(::BaseEncoders, ConcreteClasses) }
        generate("GeneratedDumperProperties") { generateDumperProperties(ConcreteClasses) }
    }
}

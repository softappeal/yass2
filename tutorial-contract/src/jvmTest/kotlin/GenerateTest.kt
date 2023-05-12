package ch.softappeal.yass2.tutorial.contract

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.generate.manual.*
import kotlin.io.path.*
import kotlin.test.*

class GenerateTest {
    @Test
    fun test() {
        fun generate(fileName: String, code: Appendable.() -> Unit) = generate(Mode.Verify, Path("src/commonMain/kotlin"), Person::class.java.packageName, fileName, code)
        generate(GENERATED_PROXY) { generateProxy(Services) }
        generate(GENERATED_BINARY_SERIALIZER) { generateBinarySerializer(::BaseEncoders, ConcreteClasses) }
        generate(GENERATED_DUMPER_PROPERTIES) { generateDumperProperties(ConcreteClasses) }
    }
}

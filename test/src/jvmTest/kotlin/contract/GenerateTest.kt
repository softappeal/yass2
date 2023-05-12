package ch.softappeal.yass2.contract

import ch.softappeal.yass2.ksp.*
import ch.softappeal.yass2.remote.coroutines.*
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

    @Test
    fun testFlowService() {
        fun generate(fileName: String, code: Appendable.() -> Unit) = generate(Path("../yass2-coroutines/src/commonMain/kotlin/remote/coroutines"), "ch.softappeal.yass2.remote.coroutines", fileName, code)
        generate("GeneratedProxy") { generateProxy(listOf(FlowService::class)) }
    }
}

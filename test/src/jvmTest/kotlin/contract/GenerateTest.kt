package ch.softappeal.yass2.contract

import ch.softappeal.yass2.generate.*
import kotlin.io.path.*
import kotlin.test.*

private fun generate(fileName: String, code: String) {
    GenerateAction.Verify.execute(
        Path("src/commonTest/kotlin/contract/$fileName"),
        "package ch.softappeal.yass2.contract\n\n$code",
    )
}

class GenerateTest {
    @Test
    fun generateProxyFactory() {
        generate("ContractProxyFactory.kt", generateProxyFactory("ContractProxyFactory", ServiceIds.map { it.service } + Mixed::class))
    }

    @Test
    fun generateRemote() {
        generate("ContractRemote.kt", generateRemoteProxyFactory("ContractRemoteProxyFactory", ServiceIds) + "\n" + generateInvoke("contractInvoke", ServiceIds))
    }

    @Test
    fun generateBinarySerializer() {
        generate("ContractBinarySerializer.kt", generateBinarySerializer("ContractBinarySerializer", ::BaseEncoders, TreeConcreteClasses, GraphConcreteClasses))
    }

    @Test
    fun generateDumper() {
        generate("ContractDumperProperties.kt", generateDumperProperties("ContractDumperProperties", TreeConcreteClasses + GraphConcreteClasses))
    }
}

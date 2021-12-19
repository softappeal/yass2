package ch.softappeal.yass2.contract

import ch.softappeal.yass2.generate.*
import kotlin.test.*

private fun generate(fileName: String, code: String) {
    GenerateAction.Verify.execute(
        "src/commonTest/kotlin/contract/generated/$fileName",
        "package ch.softappeal.yass2.contract.generated\n\n$code",
    )
}

class GenerateTest {
    @Test
    fun generateProxyFactory() {
        generate("GeneratedProxyFactory.kt", generateProxyFactory(ServiceIds.map { it.service }))
    }

    @Test
    fun generateRemote() {
        generate("GeneratedRemote.kt", generateRemoteProxyFactory(ServiceIds) + "\n" + generateInvoke(ServiceIds))
    }

    @Test
    fun generateBinarySerializer() {
        generate("GeneratedBinarySerializer.kt", generateBinarySerializer(::baseEncoders, ConcreteClasses))
    }

    @Test
    fun generateDumper() {
        generate("GeneratedDumperProperties.kt", generateDumperProperties(ConcreteClasses))
    }
}

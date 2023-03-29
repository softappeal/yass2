package ch.softappeal.yass2.contract

import ch.softappeal.yass2.generate.*
import kotlin.io.path.*
import kotlin.test.*

private fun generate(fileName: String, code: String) {
    GenerateAction.Verify.execute(
        Path("src/commonTest/kotlin/contract/generated/$fileName"),
        "package ch.softappeal.yass2.contract.generated\n\n$code",
    )
}

class GenerateTest {
    @Test
    fun generateProxyFactory() {
        generate("GeneratedProxyFactory.kt", generateProxyFactory(ServiceIds.map { it.service } + Mixed::class))
    }

    @Test
    fun generateRemote() {
        generate("GeneratedRemote.kt", generateRemoteProxyFactory(ServiceIds) + "\n" + generateInvoke(ServiceIds))
    }

    @Test
    fun generateBinarySerializer() {
        generate("GeneratedBinarySerializer.kt", generateBinarySerializer(BaseEncoders, TreeConcreteClasses, GraphConcreteClasses))
    }

    @Test
    fun generateDumper() {
        generate("GeneratedDumperProperties.kt", generateDumperProperties(TreeConcreteClasses + GraphConcreteClasses))
    }
}

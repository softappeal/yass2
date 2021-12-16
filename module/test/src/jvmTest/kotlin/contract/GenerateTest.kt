package ch.softappeal.yass2.contract

import ch.softappeal.yass2.generate.*
import java.io.*
import kotlin.test.*

private fun generate(fileName: String, code: String) {
    val text = "package ch.softappeal.yass2.contract.generated\n\n$code"
    print(text)
    val filePath = "src/commonTest/kotlin/contract/generated/$fileName"
    assertEquals(text, File(filePath).readText().replace("\r\n", "\n"))
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
        generate("GeneratedBinarySerializer.kt", generateBinarySerializer(BaseEncoders, ConcreteClasses))
    }

    @Test
    fun generateDumper() {
        generate("GeneratedDumperProperties.kt", generateDumperProperties(ConcreteClasses))
    }
}

package ch.softappeal.yass2.tutorial.generate

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.tutorial.contract.*
import java.io.*

private fun generate(fileName: String, code: String) {
    val text = "package ch.softappeal.yass2.tutorial.contract.generated\n\n$code"
    val filePath = "src/commonMain/kotlin/contract/generated/$fileName"
    check(text == File(filePath).readText().replace("\r\n", "\n")) // only needed for validating files
    // File(filePath).writeText(text)
}

fun main() {
    generate("GeneratedProxyFactory.kt", generateProxyFactory(ServiceIds.map { it.service }))
    generate("GeneratedRemote.kt", generateRemoteProxyFactory(ServiceIds) + "\n" + generateInvoke(ServiceIds))
    generate("GeneratedBinarySerializer.kt", generateBinarySerializer(BaseEncoders, ConcreteClasses))
    generate("GeneratedDumper.kt", generateDumperProperties(ConcreteClasses))
}

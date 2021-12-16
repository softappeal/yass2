package ch.softappeal.yass2.tutorial.generate

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.tutorial.contract.*
import java.io.*

private enum class Action { Generate, Verify }

private fun generate(fileName: String, code: String) {
    val text = "package ch.softappeal.yass2.tutorial.contract.generated\n\n$code"
    val filePath = "src/commonMain/kotlin/contract/generated/$fileName"
    when (Action.Verify) {
        Action.Generate -> File(filePath).writeText(text)
        Action.Verify -> check(text == File(filePath).readText().replace("\r\n", "\n"))
    }
}

fun main() {
    generate("GeneratedProxyFactory.kt", generateProxyFactory(ServiceIds.map { it.service }))
    generate("GeneratedRemote.kt", generateRemoteProxyFactory(ServiceIds) + "\n" + generateInvoke(ServiceIds))
    generate("GeneratedBinarySerializer.kt", generateBinarySerializer(baseEncoders(), ConcreteClasses))
    generate("GeneratedDumperProperties.kt", generateDumperProperties(ConcreteClasses))
}

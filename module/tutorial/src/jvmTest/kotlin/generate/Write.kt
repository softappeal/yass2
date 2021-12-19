package ch.softappeal.yass2.tutorial.generate

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.tutorial.contract.*

private fun generate(fileName: String, code: String) {
    GenerateAction.Verify/* or Write */.execute(
        "src/commonMain/kotlin/contract/generated/$fileName",
        "package ch.softappeal.yass2.tutorial.contract.generated\n\n$code",
    )
}

fun main() {
    generate("GeneratedProxyFactory.kt", generateProxyFactory(ServiceIds.map { it.service }))
    generate("GeneratedRemote.kt", generateRemoteProxyFactory(ServiceIds) + "\n" + generateInvoke(ServiceIds))
    generate("GeneratedBinarySerializer.kt", generateBinarySerializer(::baseEncoders, ConcreteClasses))
    generate("GeneratedDumperProperties.kt", generateDumperProperties(ConcreteClasses))
}

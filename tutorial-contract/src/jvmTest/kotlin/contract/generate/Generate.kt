package ch.softappeal.yass2.tutorial.contract.generate

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.tutorial.contract.*

internal fun GenerateAction.execute() {
    all(
        "src/commonMain/kotlin/contract/generated",
        "ch.softappeal.yass2.tutorial.contract.generated",
        ServiceIds,
        BaseEncoders, ConcreteClasses,
    )
}

fun main() {
    GenerateAction.Write.execute()
}

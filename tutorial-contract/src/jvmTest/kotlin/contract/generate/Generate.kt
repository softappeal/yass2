package ch.softappeal.yass2.tutorial.contract.generate

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.tutorial.contract.*
import kotlin.io.path.*

internal fun GenerateAction.execute() {
    all(
        "tutorial",
        Path("src/commonMain/kotlin/contract"),
        "ch.softappeal.yass2.tutorial.contract",
        ServiceIds,
        ::BaseEncoders, ConcreteClasses,
    )
}

fun main() {
    GenerateAction.Write.execute()
}

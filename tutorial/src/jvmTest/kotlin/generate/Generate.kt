package ch.softappeal.yass2.tutorial.generate

import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.tutorial.contract.*

fun main(): Unit = GenerateAction.Verify/* or Write */.all(
    "src/commonMain/kotlin/contract/generated", "ch.softappeal.yass2.tutorial.contract.generated",
    ServiceIds,
    BaseEncoders, ConcreteClasses,
)

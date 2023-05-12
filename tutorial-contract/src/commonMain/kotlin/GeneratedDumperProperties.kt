@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "RedundantNullableReturnType",
    "KotlinRedundantDiagnosticSuppress",
    "RedundantSuppression",
)

package ch.softappeal.yass2.tutorial.contract

public val GeneratedDumperProperties: ch.softappeal.yass2.DumperProperties = ch.softappeal.yass2.dumperProperties(
    ch.softappeal.yass2.tutorial.contract.Address::class to listOf(
        ch.softappeal.yass2.tutorial.contract.Address::number as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.Address::street as kotlin.reflect.KProperty1<Any, Any?>,
    ),
    ch.softappeal.yass2.tutorial.contract.Person::class to listOf(
        ch.softappeal.yass2.tutorial.contract.Person::addresses as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.Person::gender as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.Person::name as kotlin.reflect.KProperty1<Any, Any?>,
    ),
    ch.softappeal.yass2.tutorial.contract.DivideByZeroException::class to listOf(
    ),
    ch.softappeal.yass2.tutorial.contract.SubClass::class to listOf(
        ch.softappeal.yass2.tutorial.contract.SubClass::baseClassProperty as kotlin.reflect.KProperty1<Any, Any?>,
        ch.softappeal.yass2.tutorial.contract.SubClass::subClassProperty as kotlin.reflect.KProperty1<Any, Any?>,
    ),
    ch.softappeal.yass2.tutorial.contract.BooleanFlowId::class to listOf(
    ),
    ch.softappeal.yass2.tutorial.contract.IntFlowId::class to listOf(
        ch.softappeal.yass2.tutorial.contract.IntFlowId::max as kotlin.reflect.KProperty1<Any, Any?>,
    ),
)

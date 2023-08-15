@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "unused",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "RedundantNullableReturnType",
    "KotlinRedundantDiagnosticSuppress",
    "RedundantSuppression",
)

package ch.softappeal.yass2.contract.generated

public fun createDumper(dumpValue: kotlin.text.Appendable.(value: kotlin.Any) -> kotlin.Unit): ch.softappeal.yass2.Dumper =
    ch.softappeal.yass2.createDumper(
        ch.softappeal.yass2.dumperProperties(
            ch.softappeal.yass2.contract.IntException::class to listOf(
                ch.softappeal.yass2.contract.IntException::i as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.PlainId::class to listOf(
                ch.softappeal.yass2.contract.PlainId::id as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.ComplexId::class to listOf(
                ch.softappeal.yass2.contract.ComplexId::baseId as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::baseIdOptional as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::id as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::plainId as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::plainIdOptional as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Lists::class to listOf(
                ch.softappeal.yass2.contract.Lists::list as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.Lists::listOptional as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.Lists::mutableList as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Id2::class to listOf(
                ch.softappeal.yass2.contract.Id2::id as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Id3::class to listOf(
                ch.softappeal.yass2.contract.Id3::id as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.IdWrapper::class to listOf(
                ch.softappeal.yass2.contract.IdWrapper::id as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.IdWrapper::idOptional as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.ManyProperties::class to listOf(
                ch.softappeal.yass2.contract.ManyProperties::a as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::b as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::c as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::d as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::e as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::f as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::g as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::h as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::i as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::j as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.DivideByZeroException::class to listOf(
            ),
            ch.softappeal.yass2.contract.ThrowableFake::class to listOf(
                ch.softappeal.yass2.contract.ThrowableFake::cause as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ThrowableFake::message as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Node::class to listOf(
                ch.softappeal.yass2.contract.Node::id as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.Node::link as kotlin.reflect.KProperty1<Any, Any?>,
            ),
        ),
        setOf(
            ch.softappeal.yass2.contract.Node::class,
        ),
        dumpValue,
    )

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

package ch.softappeal.yass2.contract.child.reflect

public fun ch.softappeal.yass2.contract.child.NoSuspend.proxy(
    intercept: ch.softappeal.yass2.Interceptor,
): ch.softappeal.yass2.contract.child.NoSuspend = object : ch.softappeal.yass2.contract.child.NoSuspend {
    override fun x(
    ) {
        intercept(ch.softappeal.yass2.contract.child.NoSuspend::x, listOf()) {
            this@proxy.x()
        }
    }
}

public fun createDumper(dumpValue: ch.softappeal.yass2.ValueDumper): ch.softappeal.yass2.Dumper =
    ch.softappeal.yass2.createDumper(
        ch.softappeal.yass2.dumperProperties(
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
